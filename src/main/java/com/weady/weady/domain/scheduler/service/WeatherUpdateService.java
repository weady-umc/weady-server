package com.weady.weady.domain.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weady.weady.common.constant.WeatherApiProperties;
import com.weady.weady.common.error.errorCode.KMAErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.SkyCode;
import com.weady.weady.domain.weather.repository.WeatherShortDetailRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;                  // NEW
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;      // NEW
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WeatherUpdateService {

    private static final long REQ_INTERVAL_MS = 600L; // 직렬 호출 간 텀
    private static final int MAX_RETRY = 4;            // 레이트리밋/네트워크 재시도 횟수
    private static final int CONCURRENCY = 3;          // NEW: 동시에 처리할 그리드 수(2~4 추천)
    private static final int FORECAST_WINDOW_HOURS = 36; // 저장할 예보 범위(시간)

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final LocationRepository locationRepository;
    private final WeatherShortDetailRepository weatherRepository;
    private final WeatherApiProperties apiProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 동시 실행 차단
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final TransactionTemplate txTemplate;
    private final Object rateLock = new Object();
    private volatile long lastRequestAtMs = 0L;

    public WeatherUpdateService(LocationRepository locationRepository,
                                WeatherShortDetailRepository weatherRepository,
                                WeatherApiProperties apiProperties,
                                @Qualifier("kmaWebClient") WebClient webClient,
                                PlatformTransactionManager txManager) {
        this.locationRepository = locationRepository;
        this.weatherRepository = weatherRepository;
        this.apiProperties = apiProperties;
        this.webClient = webClient;
        this.txTemplate = new TransactionTemplate(txManager);
    }

    private void throttleGlobal() {
        synchronized (rateLock) {
            while (true) {
                long now = System.currentTimeMillis();
                long wait = lastRequestAtMs + REQ_INTERVAL_MS - now;
                if (wait <= 0) {
                    lastRequestAtMs = now;
                    rateLock.notifyAll();
                    return;
                }
                try { rateLock.wait(wait); } catch (InterruptedException ignored) {}
            }
        }
    }

    @Async("weatherTaskExecutor")
    public void updateShortTermWeather() {
        if (!running.compareAndSet(false, true)) {
            log.warn("이미 실행 중이라 스킵");
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            LocalDateTime nowKst = LocalDateTime.now(KST);
            BaseDateTime baseDateTimeForSave = calculateBaseDateTimeKst(nowKst);

            int currentDate = Integer.parseInt(nowKst.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            int currentTime = Integer.parseInt(nowKst.format(DateTimeFormatter.ofPattern("HHmm")));
            txTemplate.executeWithoutResult(status ->
                    weatherRepository.deleteOldRecords(currentDate, currentTime)
            );
            log.info("오래된 단기예보 데이터 삭제 완료");

            List<Location> locations = locationRepository.findAll();
            if (locations.isEmpty()) {
                log.warn("업데이트할 지역 정보가 없습니다.");
                return;
            }

            // (nx, ny) 기준으로 그룹핑 → 그룹당 1회만 호출
            Map<Grid, List<Location>> byGrid = locations.stream()
                    .collect(Collectors.groupingBy(l -> new Grid(l.getNx(), l.getNy())));

            int totalGrids = byGrid.size();
            log.info("고유 그리드 수: {}", totalGrids);
            var pool = Executors.newFixedThreadPool(CONCURRENCY);
            var processed = new AtomicInteger(0);
            List<CompletableFuture<Void>> tasks = new ArrayList<>();

            for (Map.Entry<Grid, List<Location>> entry : byGrid.entrySet()) {
                tasks.add(CompletableFuture.runAsync(() -> {
                    List<Location> group = entry.getValue();
                    Location repr = group.get(0);

                    throttleGlobal();

                    URI uri = buildApiUri(repr, baseDateTimeForSave);
                    log.debug("KMA URI(grid {}): {}", entry.getKey(), uri);

                    Map<String, LocationWeatherShortDetail> reprResult =
                            fetchWeatherWithWebClient(uri, repr, baseDateTimeForSave).join();

                    if (reprResult.isEmpty()) {
                        int c = processed.incrementAndGet();
                        if (c % 50 == 0) log.info("그리드 진행 {}/{}", c, totalGrids);
                        return;
                    }

                    Map<String, LocationWeatherShortDetail> groupMap = new HashMap<>(reprResult);
                    if (group.size() > 1) {
                        for (int i = 1; i < group.size(); i++) {
                            Location other = group.get(i);
                            for (LocationWeatherShortDetail v : reprResult.values()) {
                                LocationWeatherShortDetail clone = LocationWeatherShortDetail.builder()
                                        .location(other)
                                        .observationDate(v.getObservationDate())
                                        .observationTime(v.getObservationTime())
                                        .date(v.getDate())
                                        .time(v.getTime())
                                        .tmp(v.getTmp())
                                        .wsd(v.getWsd())
                                        .skyCode(v.getSkyCode())
                                        .pop(v.getPop())
                                        .pcp(v.getPcp())
                                        .reh(v.getReh())
                                        .vec(v.getVec())
                                        .build();
                                String mapKey = other.getId() + "-" + v.getDate() + "-" + v.getTime();
                                groupMap.put(mapKey, clone);
                            }
                        }
                    }
                    saveWeatherForecastsForGroup(groupMap, group, baseDateTimeForSave);

                    int c = processed.incrementAndGet();
                    if (c % 50 == 0) log.info("그리드 진행 {}/{}", c, totalGrids);
                }, pool));
            }

            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
            pool.shutdown();

            long endTime = System.currentTimeMillis();
            log.info("총 {}개 location(그리드 {}개) 단기예보 업데이트 완료. 소요 {}ms",
                    locations.size(), totalGrids, (endTime - startTime));

        } finally {
            running.set(false);
        }
    }

    private CompletableFuture<Map<String, LocationWeatherShortDetail>> fetchWeatherWithWebClient(
            URI uri, Location location, BaseDateTime baseDateTime) {

        Mono<String> responseMono = webClient.get()
                .uri(uri)
                .header("User-Agent", "weady-weather-bot/1.0")
                .header("Accept", "application/json")
                .header("Accept-Charset", "utf-8")
                .retrieve()
                .onStatus(s -> s.is5xxServerError() || s.value() == 429, resp -> {
                    log.warn("KMA {} 응답. status={} locId={}", resp.statusCode().is5xxServerError() ? "5xx" : "429",
                            resp.statusCode(), location.getId());
                    if (resp.statusCode().value() == 429) {
                        return Mono.error(new BusinessException(KMAErrorCode.RATE_LIMIT_EXCEEDED));
                    }
                    return resp.createException().flatMap(Mono::error);
                })
                .bodyToMono(String.class)
                // XML/HTML 에러 응답 검사 및 코드 파싱
                .flatMap(body -> {
                    String trimmed = body == null ? "" : body.trim();
                    if (!trimmed.startsWith("<")) return Mono.just(body);

                    String code = extractXmlTag(trimmed, "resultCode");
                    String msg  = extractXmlTag(trimmed, "resultMsg");
                    log.warn("locId={} XML/HTML 응답: code={}, msg={}, snippet={}",
                            location.getId(), code, msg, snippet(trimmed));
                    if ("22".equals(code)) {
                        return Mono.error(new BusinessException(KMAErrorCode.RATE_LIMIT_EXCEEDED));
                    } else if ("30".equals(code) || "31".equals(code) || "32".equals(code)) {
                        return Mono.error(new ServiceKeyException(code, msg));
                    }
                    return Mono.error(new BusinessException(KMAErrorCode.KMA_XML_ERROR, msg + " (code=" + code + ")"));
                })

                .flatMap(body -> {
                    try {
                        JsonNode root = objectMapper.readTree(body);
                        String rc = root.path("response").path("header").path("resultCode").asText("");
                        String rm = root.path("response").path("header").path("resultMsg").asText("");
                        if (!"00".equals(rc)) {
                            log.warn("locId={} KMA JSON resultCode={}({}) - 스킵", location.getId(), rc, rm);
                            return Mono.empty();
                        }
                        return Mono.just(body);
                    } catch (Exception e) {
                        log.error("locId={} JSON 헤더 파싱 실패 - 스킵", location.getId(), e);
                        return Mono.empty();
                    }
                })
                // 백오프 재시도: 5xx/네트워크/레이트리밋만
                .retryWhen(
                        Retry.backoff(MAX_RETRY, Duration.ofSeconds(2))
                                .maxBackoff(Duration.ofSeconds(30))
                                .jitter(0.3)
                                .filter(th ->
                                        (th instanceof WebClientResponseException w && w.getStatusCode().is5xxServerError())
                                                || !(th instanceof ServiceKeyException)
                                )
                )
                .onErrorResume(e -> {
                    if (e instanceof ServiceKeyException se) {
                        log.error("ServiceKey 오류(code={}, msg={}) - 즉시 중단 권장", se.code, se.msg);
                    } else {
                        log.error("WebClient 오류(locId={}): {}", location.getId(), e.toString());
                    }
                    return Mono.empty();
                });

        return responseMono
                .flatMap(responseBody -> {
                    try {
                        Map<String, LocationWeatherShortDetail> parsed =
                                parseWeatherResponse(responseBody, location, baseDateTime);
                        return Mono.just(parsed);
                    } catch (JsonProcessingException e) {
                        String snippet = responseBody == null ? "null" : responseBody.substring(0, Math.min(responseBody.length(), 200));
                        log.error("Location ID {} ({}, {}) JSON 파싱 실패. 일부 본문: {}",
                                location.getId(), location.getNx(), location.getNy(), snippet, e);
                        return Mono.just(Collections.<String, LocationWeatherShortDetail>emptyMap());
                    }
                })
                .defaultIfEmpty(Collections.<String, LocationWeatherShortDetail>emptyMap())
                .toFuture();
    }

    // NEW: 그리드(같은 nx,ny 묶음)만 저장
    private void saveWeatherForecastsForGroup(Map<String, LocationWeatherShortDetail> newForecastsMap,
                                              List<Location> group,
                                              BaseDateTime baseDateTime) {
        List<Long> locationIds = group.stream().map(Location::getId).toList();
        int observationDate = Integer.parseInt(baseDateTime.baseDate);

        txTemplate.executeWithoutResult(status -> {
            List<LocationWeatherShortDetail> existingRecords =
                    weatherRepository.findExistingRecords(locationIds, observationDate);

            Map<String, LocationWeatherShortDetail> existingRecordsMap = existingRecords.stream()
                    .collect(Collectors.toMap(
                            r -> r.getLocation().getId() + "-" + r.getDate() + "-" + r.getTime(),
                            r -> r
                    ));

            List<LocationWeatherShortDetail> toInsert = new ArrayList<>();
            List<LocationWeatherShortDetail> toUpdate = new ArrayList<>();

            newForecastsMap.forEach((key, newRecord) -> {
                LocationWeatherShortDetail existingRecord = existingRecordsMap.get(key);
                if (existingRecord != null) {
                    updateEntity(existingRecord, newRecord);
                    toUpdate.add(existingRecord);
                } else {
                    toInsert.add(newRecord);
                }
            });

            if (!toInsert.isEmpty()) {
                weatherRepository.saveAll(toInsert);
                log.debug("그룹 INSERT {}건", toInsert.size());
            }
            if (!toUpdate.isEmpty()) {
                weatherRepository.saveAll(toUpdate);
                log.debug("그룹 UPDATE {}건", toUpdate.size());
            }
        });
    }

    private void updateEntity(LocationWeatherShortDetail oldEntity, LocationWeatherShortDetail newEntity) {
        oldEntity.setTmp(newEntity.getTmp());
        oldEntity.setWsd(newEntity.getWsd());
        oldEntity.setSkyCode(newEntity.getSkyCode());
        oldEntity.setPop(newEntity.getPop());
        oldEntity.setPcp(newEntity.getPcp());
        oldEntity.setReh(newEntity.getReh());
        oldEntity.setVec(newEntity.getVec());
    }

    private Map<String, LocationWeatherShortDetail> parseWeatherResponse(String jsonResponse, Location location, BaseDateTime baseDateTime) throws JsonProcessingException {
        if (jsonResponse == null || jsonResponse.trim().startsWith("<")) {
            log.error("Location ID {} ({}, {}) API가 XML/HTML 에러를 반환했습니다. 응답 내용: {}",
                    location.getId(), location.getNx(), location.getNy(), snippet(jsonResponse));
            return new HashMap<>();
        }

        Map<String, WeatherValues> weatherValuesMap = new HashMap<>();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        if (items.isMissingNode() || !items.isArray()) {
            log.warn("Location ID {}: 유효하지 않은 API 응답 (items 없음). 응답: {}",
                    location.getId(),
                    snippet(jsonResponse));
            return new HashMap<>();
        }

        LocalDateTime startForecastTime = LocalDateTime.parse(baseDateTime.baseDate + baseDateTime.baseTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        LocalDateTime endForecastTime = startForecastTime.plusHours(FORECAST_WINDOW_HOURS);

        items.forEach(item -> {
            String fcstDate = item.path("fcstDate").asText();
            String fcstTime = item.path("fcstTime").asText();
            LocalDateTime forecastDateTime = LocalDateTime.parse(fcstDate + fcstTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

            // 시작 이전/끝 이후 데이터는 제외
            if (forecastDateTime.isBefore(startForecastTime) || forecastDateTime.isAfter(endForecastTime)) {
                return;
            }

            String key = fcstDate + "-" + fcstTime;
            WeatherValues values = weatherValuesMap.computeIfAbsent(key, k -> new WeatherValues());
            String category = item.path("category").asText();
            String fcstValue = item.path("fcstValue").asText();

            switch (category) {
                case "TMP": values.tmp = parseFloatSafe(fcstValue); break;
                case "WSD": values.wsd = parseFloatSafe(fcstValue); break;
                case "POP": values.pop = parseFloatSafe(fcstValue); break;
                case "PCP": values.pcp = parsePcp(fcstValue); break;
                case "REH": values.reh = parseFloatSafe(fcstValue); break;
                case "VEC": values.vec = parseFloatSafe(fcstValue); break;
                case "SKY": values.skyCode = fcstValue; break;
                case "PTY": values.ptyCode = fcstValue; break;
            }
        });

        Map<String, LocationWeatherShortDetail> resultMap = new HashMap<>();
        weatherValuesMap.forEach((key, values) -> {
            String[] dateTimeParts = key.split("-");
            int date = Integer.parseInt(dateTimeParts[0]);
            int time = Integer.parseInt(dateTimeParts[1]);
            SkyCode finalSkyCode = SkyCode.fromKmaCodes(values.ptyCode, values.skyCode);

            LocationWeatherShortDetail detail = LocationWeatherShortDetail.builder()
                    .location(location)
                    .observationDate(Integer.parseInt(baseDateTime.baseDate))
                    .observationTime(Integer.parseInt(baseDateTime.baseTime))
                    .date(date).time(time).tmp(values.tmp).wsd(values.wsd)
                    .skyCode(finalSkyCode).pop(values.pop).pcp(values.pcp)
                    .reh(values.reh).vec(values.vec).build();
            String mapKey = location.getId() + "-" + date + "-" + time;
            resultMap.put(mapKey, detail);
        });
        return resultMap;
    }

    private Float parseFloatSafe(String v) {
        try { return Float.parseFloat(v); } catch (Exception e) { return null; }
    }

    private float parsePcp(String v) {
        if (v == null) return 0f;
        v = v.trim();
        if (v.equals("강수없음")) return 0f;
        if (v.contains("1mm 미만")) return 0.5f;
        String only = v.replaceAll("[^\\d.]", "");
        if (only.isEmpty()) return 0f;
        try { return Float.parseFloat(only); } catch (Exception e) { return 0f; }
    }

    private URI buildApiUri(Location location, BaseDateTime baseDateTime) {
        String encodedKey = URLEncoder.encode(apiProperties.getShortTermKey(), StandardCharsets.UTF_8);

        return UriComponentsBuilder.fromUriString(apiProperties.getBaseUrl())
                .queryParam("serviceKey", encodedKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDateTime.baseDate)
                .queryParam("base_time", baseDateTime.baseTime)
                .queryParam("nx", location.getNx())
                .queryParam("ny", location.getNy())
                .build(true)
                .toUri();
    }

    private BaseDateTime calculateBaseDateTimeKst(LocalDateTime nowKst) {
        int hour = nowKst.getHour();
        int minute = nowKst.getMinute();
        int[] baseHours = {2, 5, 8, 11, 14, 17, 20, 23};

        LocalDateTime baseDateTime = nowKst;
        int targetHour = baseHours[0];

        if (hour < 2 || (hour == 2 && minute < 10)) {
            baseDateTime = nowKst.minusDays(1);
            targetHour = 23;
        } else {
            for (int i = baseHours.length - 1; i >= 0; i--) {
                if (hour > baseHours[i] || (hour == baseHours[i] && minute >= 10)) {
                    targetHour = baseHours[i];
                    break;
                }
            }
        }

        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = String.format("%02d00", targetHour);
        return new BaseDateTime(baseDate, baseTime);
    }

    private static String extractXmlTag(String xml, String tag) {
        Pattern p = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">", Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        return m.find() ? m.group(1).trim() : "";
    }

    private static String snippet(String s) {
        if (s == null) return "null";
        String x = s.replaceAll("\\s+", " ").trim();
        return x.length() > 400 ? x.substring(0, 400) + "…" : x;
    }

    private record BaseDateTime(String baseDate, String baseTime) {}

    private static class WeatherValues {
        Float tmp, wsd, pop, pcp, reh, vec;
        String ptyCode = "0";
        String skyCode;
    }

    private record Grid(int nx, int ny) {}

    private static class ServiceKeyException extends RuntimeException {
        final String code; final String msg;
        ServiceKeyException(String code, String msg) { super(code + ":" + msg); this.code = code; this.msg = msg; }
    }
}
