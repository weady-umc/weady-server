package com.weady.weady.domain.weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weady.weady.common.constant.WeatherApiProperties;
import com.weady.weady.common.error.errorCode.KMAErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.domain.location.entity.GridLocation;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.SkyCode;
import com.weady.weady.domain.weather.repository.GridLocationRepository;
import com.weady.weady.domain.weather.repository.WeatherShortDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShortWeatherSchedulerService {

    @Value("${weady.kma.req-interval-ms:400}")    private long reqIntervalMs;
    @Value("${weady.kma.concurrency:3}")          private int concurrency;
    @Value("${weady.kma.forecast-window-hours:29}") private int forecastWindowHours;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final GridLocationRepository gridLocationRepository;
    private final WeatherShortDetailRepository weatherRepository;
    private final WeatherApiProperties apiProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final TransactionTemplate tx;
    private final Object rateLock = new Object();
    private volatile long lastRequestAtMs = 0L;

    public ShortWeatherSchedulerService(GridLocationRepository gridLocationRepository,
                                        WeatherShortDetailRepository weatherRepository,
                                        WeatherApiProperties apiProperties,
                                        @Qualifier("kmaWebClient") WebClient webClient,
                                        TransactionTemplate tx) {
        this.gridLocationRepository = gridLocationRepository;
        this.weatherRepository = weatherRepository;
        this.apiProperties = apiProperties;
        this.webClient = webClient;
        this.tx = tx;
    }

    private void throttleGlobal() {
        synchronized (rateLock) {
            while (true) {
                long now = System.currentTimeMillis();
                long wait = lastRequestAtMs + reqIntervalMs - now;
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
            log.warn("Skipping execution");
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            LocalDateTime nowKst = LocalDateTime.now(KST);
            BaseDateTime baseDateTimeForSave = calculateBaseDateTimeKst(nowKst);

            int currentDate = Integer.parseInt(nowKst.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            int currentTime = Integer.parseInt(nowKst.format(DateTimeFormatter.ofPattern("HHmm")));

            tx.executeWithoutResult(status -> weatherRepository.deleteOldRecords(currentDate, currentTime));
            log.info("Deleted old weather records");

            List<GridLocation> gridLocations = gridLocationRepository.findAll();
            if (gridLocations.isEmpty()) {
                log.warn("No grid locations found");
                return;
            }

            int totalGrids = gridLocations.size();
            log.info("Total grids: {}", totalGrids);

            var pool = Executors.newFixedThreadPool(Math.max(1, concurrency));
            var processed = new AtomicInteger(0);
            List<CompletableFuture<Void>> tasks = new ArrayList<>();

            for (GridLocation grid : gridLocations) {
                tasks.add(CompletableFuture.runAsync(() -> {
                    throttleGlobal();

                    long t1 = System.nanoTime();
                    URI uri = buildApiUri(grid, baseDateTimeForSave);

                    Map<String, LocationWeatherShortDetail> resultApiMap =
                            fetchWeatherWithWebClient(uri, grid, baseDateTimeForSave).join();
                    long t2 = System.nanoTime();

                    if (resultApiMap.isEmpty()) {
                        logResult(processed, totalGrids, t1, t2, 0, 0);
                        return;
                    }

                    long t3BeforeDb = System.nanoTime();
                    saveWeatherForecasts(resultApiMap, grid, baseDateTimeForSave);
                    long t4 = System.nanoTime();

                    logResult(processed, totalGrids, t1, t2, t3BeforeDb, t4);
                }, pool));
            }

            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
            pool.shutdown();

            long endTime = System.currentTimeMillis();
            log.info("Update completed. Total time: {}ms", (endTime - startTime));

        } finally {
            running.set(false);
        }
    }

    private void logResult(AtomicInteger processed, int total, long t1, long t2, long t3, long t4) {
        int c = processed.incrementAndGet();
        if (c % 50 == 0) log.info("Progress {}/{}", c, total);
        long apiTime = (t2 - t1) / 1_000_000;
        long dbTime = (t3 == 0) ? 0 : (t4 - t3) / 1_000_000;
        log.debug("Grid processed. API={}ms DB={}ms", apiTime, dbTime);
    }

    private CompletableFuture<Map<String, LocationWeatherShortDetail>> fetchWeatherWithWebClient(
            URI uri, GridLocation grid, BaseDateTime baseDateTime) {

        Mono<String> responseMono = webClient.get()
                .uri(uri)
                .header("User-Agent", "weady-weather-bot/1.0")
                .header("Accept", "application/json")
                .retrieve()
                .onStatus(s -> s.is5xxServerError() || s.value() == 429, resp -> {
                    if (resp.statusCode().value() == 429) {
                        return Mono.error(new BusinessException(KMAErrorCode.RATE_LIMIT_EXCEEDED));
                    }
                    return resp.createException().flatMap(Mono::error);
                })
                .bodyToMono(String.class)
                .flatMap(body -> {
                    String trimmed = body == null ? "" : body.trim();
                    if (!trimmed.startsWith("<")) return Mono.just(body);

                    String code = extractXmlTag(trimmed, "resultCode");
                    String msg  = extractXmlTag(trimmed, "resultMsg");
                    if ("22".equals(code)) return Mono.error(new BusinessException(KMAErrorCode.RATE_LIMIT_EXCEEDED));
                    else if (List.of("30", "31", "32").contains(code)) return Mono.error(new ServiceKeyException(code, msg));
                    return Mono.error(new BusinessException(KMAErrorCode.KMA_XML_ERROR, msg + " code=" + code));
                })
                .flatMap(body -> {
                    try {
                        JsonNode root = objectMapper.readTree(body);
                        String rc = root.path("response").path("header").path("resultCode").asText("");
                        if (!"00".equals(rc)) return Mono.empty();
                        return Mono.just(body);
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                })
                .retryWhen(
                        Retry.backoff(2, Duration.ofSeconds(2))
                                .maxBackoff(Duration.ofSeconds(10))
                                .filter(th -> {
                                    if (th instanceof WebClientResponseException w && w.getStatusCode().is5xxServerError()) {
                                        return true;
                                    }
                                    if (th instanceof java.util.concurrent.TimeoutException || th instanceof java.net.SocketTimeoutException) {
                                        return true;
                                    }
                                    if (th instanceof BusinessException be && be.getErrorCode() == KMAErrorCode.KMA_XML_ERROR) {
                                        return true;
                                    }
                                    return false;
                                })
                                .onRetryExhaustedThrow((spec, signal) -> signal.failure())
                )
                .onErrorResume(e -> {
                    if (e instanceof ServiceKeyException se) {
                        log.error("ServiceKey Error code={}, msg={}", se.code, se.msg);
                    } else {
                        log.error("Grid ID {} API failed: {}", grid.getId(), e.getMessage());
                    }
                    return Mono.empty();
                });

        return responseMono
                .flatMap(responseBody -> {
                    try {
                        return Mono.just(parseWeatherResponse(responseBody, grid, baseDateTime));
                    } catch (JsonProcessingException e) {
                        log.error("Grid ID {} JSON parse error", grid.getId());
                        return Mono.just(Collections.<String, LocationWeatherShortDetail>emptyMap());
                    }
                })
                .defaultIfEmpty(Collections.emptyMap())
                .toFuture();
    }

    private void saveWeatherForecasts(Map<String, LocationWeatherShortDetail> newForecastsMap,
                                      GridLocation grid,
                                      BaseDateTime baseDateTime) {
        int observationDate = Integer.parseInt(baseDateTime.baseDate);

        tx.executeWithoutResult(status -> {
            List<LocationWeatherShortDetail> existingRecords =
                    weatherRepository.findByGridLocationIdAndObservationDate(grid.getId(), observationDate);

            Map<String, LocationWeatherShortDetail> existingRecordsMap = existingRecords.stream()
                    .collect(Collectors.toMap(
                            r -> r.getDate() + "-" + r.getTime(),
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

            if (!toInsert.isEmpty()) weatherRepository.saveAll(toInsert);
            if (!toUpdate.isEmpty()) weatherRepository.saveAll(toUpdate);
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

    private Map<String, LocationWeatherShortDetail> parseWeatherResponse(String jsonResponse, GridLocation grid, BaseDateTime baseDateTime) throws JsonProcessingException {
        if (jsonResponse == null || jsonResponse.trim().startsWith("<")) return new HashMap<>();

        Map<String, WeatherValues> weatherValuesMap = new HashMap<>();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        if (items.isMissingNode() || !items.isArray()) return new HashMap<>();

        LocalDateTime startForecastTime = LocalDateTime.parse(
                baseDateTime.baseDate + baseDateTime.baseTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        LocalDateTime endForecastTime = startForecastTime.plusHours(forecastWindowHours);

        items.forEach(item -> {
            String fcstDate = item.path("fcstDate").asText();
            String fcstTime = item.path("fcstTime").asText();
            LocalDateTime forecastDateTime = LocalDateTime.parse(fcstDate + fcstTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

            if (forecastDateTime.isBefore(startForecastTime) || forecastDateTime.isAfter(endForecastTime)) return;

            String key = fcstDate + "-" + fcstTime;
            WeatherValues values = weatherValuesMap.computeIfAbsent(key, k -> new WeatherValues());

            String category = item.path("category").asText();
            String fcstValue = item.path("fcstValue").asText();
            applyValue(values, category, fcstValue);
        });

        Map<String, LocationWeatherShortDetail> resultMap = new HashMap<>();
        weatherValuesMap.forEach((key, values) -> {
            String[] dateTimeParts = key.split("-");
            int date = Integer.parseInt(dateTimeParts[0]);
            int time = Integer.parseInt(dateTimeParts[1]);
            SkyCode finalSkyCode = SkyCode.fromKmaCodes(values.ptyCode, values.skyCode);

            LocationWeatherShortDetail detail = LocationWeatherShortDetail.builder()
                    .gridLocation(grid)
                    .observationDate(Integer.parseInt(baseDateTime.baseDate))
                    .observationTime(Integer.parseInt(baseDateTime.baseTime))
                    .date(date).time(time)
                    .tmp(values.tmp).wsd(values.wsd)
                    .skyCode(finalSkyCode).pop(values.pop).pcp(values.pcp)
                    .reh(values.reh).vec(values.vec)
                    .build();
            resultMap.put(key, detail);
        });
        return resultMap;
    }

    private void applyValue(WeatherValues v, String category, String val) {
        switch (category) {
            case "TMP" -> v.tmp = parseFloatSafe(val);
            case "WSD" -> v.wsd = parseFloatSafe(val);
            case "POP" -> v.pop = parseFloatSafe(val);
            case "PCP" -> v.pcp = parsePcp(val);
            case "REH" -> v.reh = parseFloatSafe(val);
            case "VEC" -> v.vec = parseFloatSafe(val);
            case "SKY" -> v.skyCode = val;
            case "PTY" -> v.ptyCode = val;
        }
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
        return only.isEmpty() ? 0f : Float.parseFloat(only);
    }

    private URI buildApiUri(GridLocation grid, BaseDateTime baseDateTime) {
        String encodedKey = URLEncoder.encode(apiProperties.getShortTermKey(), StandardCharsets.UTF_8);
        return UriComponentsBuilder.fromUriString(apiProperties.getBaseUrl())
                .queryParam("serviceKey", encodedKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDateTime.baseDate)
                .queryParam("base_time", baseDateTime.baseTime)
                .queryParam("nx", grid.getNx())
                .queryParam("ny", grid.getNy())
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
    private static class ServiceKeyException extends RuntimeException {
        final String code; final String msg;
        ServiceKeyException(String code, String msg) { super(code + ":" + msg); this.code = code; this.msg = msg; }
    }
}