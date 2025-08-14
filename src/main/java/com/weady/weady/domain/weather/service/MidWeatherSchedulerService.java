package com.weady.weady.domain.weather.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weady.weady.common.constant.WeatherApiProperties;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.weather.dto.scheduler.KmaResponse;
import com.weady.weady.domain.weather.dto.scheduler.MidLandItem;
import com.weady.weady.domain.weather.dto.scheduler.MidTempItem;
import com.weady.weady.domain.weather.entity.SkyCode;
import com.weady.weady.domain.weather.entity.WeatherMidDetail;
import com.weady.weady.domain.weather.repository.WeatherMidDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class MidWeatherSchedulerService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String BASE_HOST = "https://apis.data.go.kr";

    private final WebClient kmaWebClient;
    private final WeatherMidDetailRepository detailRepo;
    private final LocationRepository locationRepo;
    private final WeatherApiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MidWeatherSchedulerService(
            @Qualifier("kmaWebClient") WebClient kmaWebClient,
            WeatherMidDetailRepository detailRepo,
            LocationRepository locationRepo,
            WeatherApiProperties props
    ) {
        this.kmaWebClient = kmaWebClient;
        this.detailRepo = detailRepo;
        this.locationRepo = locationRepo;
        this.props = props;
    }

    @Transactional
    public void update() {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        String tmFc = resolveLatestTmFc(nowKst);
        LocalDate baseDay = LocalDate.parse(tmFc.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE);

        log.info("[MidWeather] tmFc={}, baseDay={}", tmFc, baseDay);

        // 수도권(육상예보 regId 예시: 11B00000)
        MidLandItem land = fetchMidLand("11B00000", tmFc);
        if (land == null) {
            log.warn("[MidWeather] MidLand null, skip");
            return;
        }

        // 오전/오후 하늘, POP 최대
        Map<Integer, SkyCode> amSky = Map.of(
                3, SkyCode.fromKmaText(land.getWf3Am()),
                4, SkyCode.fromKmaText(land.getWf4Am()),
                5, SkyCode.fromKmaText(land.getWf5Am()),
                6, SkyCode.fromKmaText(land.getWf6Am()),
                7, SkyCode.fromKmaText(land.getWf7Am())
        );
        Map<Integer, SkyCode> pmSky = Map.of(
                3, SkyCode.fromKmaText(land.getWf3Pm()),
                4, SkyCode.fromKmaText(land.getWf4Pm()),
                5, SkyCode.fromKmaText(land.getWf5Pm()),
                6, SkyCode.fromKmaText(land.getWf6Pm()),
                7, SkyCode.fromKmaText(land.getWf7Pm())
        );
        Map<Integer, Integer> popMax = new HashMap<>();
        popMax.put(3, max(land.getRnSt3Am(), land.getRnSt3Pm()));
        popMax.put(4, max(land.getRnSt4Am(), land.getRnSt4Pm()));
        popMax.put(5, max(land.getRnSt5Am(), land.getRnSt5Pm()));
        popMax.put(6, max(land.getRnSt6Am(), land.getRnSt6Pm()));
        popMax.put(7, max(land.getRnSt7Am(), land.getRnSt7Pm()));

        // 지역별 기온 regId 목록 조회
        List<String> regIds = locationRepo.findDistinctMidTermRegCodes();
        if (regIds.isEmpty()) {
            log.warn("[MidWeather] No temp regIds from Location");
            return;
        }

        int upserts = 0;
        for (String regId : regIds) {
            MidTempItem temp = fetchMidTemp(regId, tmFc);
            if (temp == null) {
                log.warn("[MidWeather] MidTemp null for regId={}", regId);
                continue;
            }

            for (int d = 3; d <= 7; d++) {
                LocalDate target = baseDay.plusDays(d);
                Float tmn = getMin(temp, d);
                Float tmx = getMax(temp, d);
                if (tmn == null || tmx == null) continue;

                SkyCode am = amSky.get(d);
                SkyCode pm = pmSky.get(d);
                Integer pop = popMax.get(d);

                upsert(regId, target,
                        pop == null ? null : pop.floatValue(),
                        am, pm, tmx, tmn);
                upserts++;
            }
        }
        log.info("[MidWeather] upserts={}", upserts);
    }

    private void upsert(String regId, LocalDate date, Float pop, SkyCode am, SkyCode pm, Float tmx, Float tmn) {
        WeatherMidDetail entity = detailRepo.findByMidTermRegCodeAndDate(regId, date)
                .orElseGet(() -> WeatherMidDetail.builder()
                        .midTermRegCode(regId)
                        .date(date)
                        .build());
        entity.apply(pop, am, pm, tmx, tmn);
        detailRepo.save(entity);
    }

    private Integer max(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.max(a, b);
    }

    private Float getMin(MidTempItem t, int d) {
        return switch (d) {
            case 3 -> toF(t.getTaMin3());
            case 4 -> toF(t.getTaMin4());
            case 5 -> toF(t.getTaMin5());
            case 6 -> toF(t.getTaMin6());
            case 7 -> toF(t.getTaMin7());
            default -> null;
        };
    }

    private Float getMax(MidTempItem t, int d) {
        return switch (d) {
            case 3 -> toF(t.getTaMax3());
            case 4 -> toF(t.getTaMax4());
            case 5 -> toF(t.getTaMax5());
            case 6 -> toF(t.getTaMax6());
            case 7 -> toF(t.getTaMax7());
            default -> null;
        };
    }

    private Float toF(Integer v) {
        return v == null ? null : v.floatValue();
    }

    private String resolveLatestTmFc(ZonedDateTime nowKst) {
        LocalDate date = nowKst.toLocalDate();
        int hour = nowKst.getHour();
        String hhmm;
        if (hour < 6) { date = date.minusDays(1); hhmm = "1800"; }
        else if (hour < 18) { hhmm = "0600"; }
        else { hhmm = "1800"; }
        return date.format(DateTimeFormatter.BASIC_ISO_DATE) + hhmm;
    }

    private MidLandItem fetchMidLand(String regId, String tmFc) {
        URI uri = buildMidApiUri("/1360000/MidFcstInfoService/getMidLandFcst", tmFc, regId);
        return kmaWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(body -> {
                    String trimmed = body == null ? "" : body.trim();

                    if (trimmed.startsWith("<")) {
                        String code = extractXmlTag(trimmed, "returnReasonCode");
                        String msg  = extractXmlTag(trimmed, "errMsg");
                        log.warn("[MidWeather] getMidLandFcst returned text/xml;charset=UTF-8, code={}, msg={}, snippet={}", code, msg, snippet(trimmed));
                        return Mono.empty();
                    }

                    try {
                        JsonNode root = objectMapper.readTree(trimmed);
                        String rc = root.path("response").path("header").path("resultCode").asText("");
                        String rm = root.path("response").path("header").path("resultMsg").asText("");
                        if (!"00".equals(rc)) {
                            log.warn("[MidWeather] midLand resultCode={}({})", rc, rm);
                            return Mono.empty();
                        }
                        JsonNode items =
                                root.path("response").path("body").path("items").path("item");
                        if (items.isArray() && !items.isEmpty()) {
                            MidLandItem item = objectMapper.treeToValue(items.get(0), MidLandItem.class);
                            return Mono.just(item);
                        }
                        return Mono.empty();
                    } catch (Exception e) {
                        log.warn("[MidWeather] midLand JSON parse error", e);
                        return Mono.empty();
                    }
                })
                .retryWhen(Retry.backoff(2, java.time.Duration.ofMillis(300)).filter(this::isRetryable))
                .onErrorResume(e -> { log.warn("[MidWeather] fetchMidLand error", e); return Mono.empty(); })
                .block();
    }

    private MidTempItem fetchMidTemp(String regId, String tmFc) {
        URI uri = buildMidApiUri("/1360000/MidFcstInfoService/getMidTa", tmFc, regId);
        return kmaWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(body -> {
                    String trimmed = body == null ? "" : body.trim();
                    if (trimmed.startsWith("<")) {
                        String code = extractXmlTag(trimmed, "returnReasonCode");
                        String msg  = extractXmlTag(trimmed, "errMsg");
                        log.warn("[MidWeather] getMidTa returned text/xml;charset=UTF-8, code={}, msg={}, snippet={}", code, msg, snippet(trimmed));
                        return Mono.empty();
                    }

                    try {
                        JsonNode root = objectMapper.readTree(trimmed);
                        String rc = root.path("response").path("header").path("resultCode").asText("");
                        String rm = root.path("response").path("header").path("resultMsg").asText("");
                        if (!"00".equals(rc)) {
                            log.warn("[MidWeather] midTa resultCode={}({})", rc, rm);
                            return Mono.empty();
                        }
                        JsonNode items =
                                root.path("response").path("body").path("items").path("item");
                        if (items.isArray() && !items.isEmpty()) {
                            MidTempItem item = objectMapper.treeToValue(items.get(0), MidTempItem.class);
                            return Mono.just(item);
                        }
                        return Mono.empty();
                    } catch (Exception e) {
                        log.warn("[MidWeather] midTa JSON parse error", e);
                        return Mono.empty();
                    }
                })
                .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofMillis(300)).filter(this::isRetryable))
                .onErrorResume(e -> { log.warn("[MidWeather] fetchMidTemp error for {}", regId, e); return Mono.empty(); })
                .block();
    }

    private URI buildMidApiUri(String path, String tmFc, String regId) {
        String key = props.getShortTermKey();
        String encodedKey = encodeIfNeeded(key);
        return UriComponentsBuilder
                .fromUriString(BASE_HOST)
                .path(path)
                .queryParam("serviceKey", encodedKey)
                .queryParam("dataType", "JSON")
                .queryParam("tmFc", tmFc)
                .queryParam("regId", regId)
                .build(true)
                .toUri();
    }

    private String encodeIfNeeded(String key) {
        if (key == null) return "";
        // 이미 퍼센트 인코딩 흔적이 있으면 그대로 사용
        return key.contains("%") ? key : URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof WebClientResponseException we) {
            int s = we.getStatusCode().value();
            return s == 429 || (s >= 500 && s < 600);
        }
        return e instanceof IOException || e instanceof TimeoutException;
    }

    private static String extractXmlTag(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i >= 0 && j > i) {
            return xml.substring(i + open.length(), j).trim();
        }
        return "";
    }

    private static String snippet(String s) {
        if (s == null) return "null";
        String x = s.replaceAll("\\s+", " ").trim();
        return x.length() > 400 ? x.substring(0, 400) + "…" : x;
    }
}
