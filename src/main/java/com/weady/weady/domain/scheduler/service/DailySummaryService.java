package com.weady.weady.domain.scheduler.service;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.tags.repository.season.SeasonRepository;
import com.weady.weady.domain.tags.repository.temperature.TemperatureRepository;
import com.weady.weady.domain.tags.repository.weather.WeatherRepository;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import com.weady.weady.domain.weather.repository.DailySummaryRepository;
import com.weady.weady.domain.weather.repository.LocationWeatherSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailySummaryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 임계값
    private static final int DAY_START = 9, DAY_END = 21; // 09~21
    private static final float POP_RAIN = 60f;
    private static final float WINDY_MS = 8f;
    private static final int RAIN_SNOW_MIN_EFFECTIVE_HOURS = 4;
    private static final int WINDY_MIN_EFFECTIVE_HOURS = 6;
    private static final float RAIN_MIN_PCP_MM = 3f;

    private final LocationRepository locationRepo;
    private final LocationWeatherSnapshotRepository snapRepo;
    private final DailySummaryRepository summaryRepo;
    private final WeatherRepository weatherTagRepo;
    private final TemperatureRepository tempTagRepo;
    private final SeasonRepository seasonTagRepo;
    private final TransactionTemplate tx;

    /** 스냅샷이 만들어진 다음날(date)의 DailySummary 생성/업데이트 */
    public void buildDailySummary(LocalDate date) {
        long t0 = System.currentTimeMillis();

        LocalDate todayKst = LocalDate.now(KST);
        int purged = tx.execute(status -> summaryRepo.deleteByReportDateBefore(todayKst));
        log.info("DailySummary 정리: {}건 삭제(date < {})", purged, todayKst);

        List<Location> locs = locationRepo.findAll();
        if (locs.isEmpty()) return;

        // 태그 캐시
        Map<String, WeatherTag> wtag = weatherTagRepo.findAll().stream()
                .collect(Collectors.toMap(WeatherTag::getName, x -> x));
        List<TemperatureTag> ttags = tempTagRepo.findAll();
        Map<String, SeasonTag> stag = seasonTagRepo.findAll().stream()
                .collect(Collectors.toMap(SeasonTag::getName, x -> x));

        // 청크 처리
        final int CHUNK = 800;
        int total = locs.size(), processed = 0, ins = 0, upd = 0;

        for (int i = 0; i < total; i += CHUNK) {
            List<Location> chunk = locs.subList(i, Math.min(i + CHUNK, total));
            List<Long> ids = chunk.stream().map(Location::getId).toList();
            // 스냅샷 조회 (00~23시, date 기준)
            List<LocationWeatherSnapshot> snaps = snapRepo.findByLocationIdsAndDate(ids, toInt(date));

            // locId -> 24시간 맵
            Map<Long, List<LocationWeatherSnapshot>> byLoc = snaps.stream()
                    .collect(Collectors.groupingBy(s -> s.getLocation().getId()));

            for (Location loc : chunk) {
                List<LocationWeatherSnapshot> day = byLoc.getOrDefault(loc.getId(), List.of());
                DailySummary ds = summarizeOne(loc, date, day, wtag, ttags, stag);
                if (ds == null) continue;

                int changed = tx.execute(status -> {
                    DailySummary old = summaryRepo.findByLocationIdAndReportDate(loc.getId(), date).orElse(null);
                    if (old == null) { summaryRepo.save(ds); return 1; }
                    // update
                    old.setSeasonTag(ds.getSeasonTag());
                    old.setWeatherTag(ds.getWeatherTag());
                    old.setTemperatureTag(ds.getTemperatureTag());
                    old.setFeelsLikeTmx(ds.getFeelsLikeTmx());
                    old.setFeelsLikeTmn(ds.getFeelsLikeTmn());
                    old.setActualTmx(ds.getActualTmx());
                    old.setActualTmn(ds.getActualTmn());
                    old.setReportDate(ds.getReportDate());
                    return 2;
                });
                if (changed == 1) ins++; else if (changed == 2) upd++;
            }

            processed += chunk.size();
            log.info("DailySummary 진행 {}/{}", processed, total);
        }
        log.info("DailySummary 완료: insert={}, update={}, 소요={}ms", ins, upd, (System.currentTimeMillis() - t0));
    }

    private DailySummary summarizeOne(
            Location loc, LocalDate date, List<LocationWeatherSnapshot> list,
            Map<String, WeatherTag> wtag, List<TemperatureTag> ttags, Map<String, SeasonTag> stag) {

        if (list.isEmpty()) return null;

        // 시간별 정렬
        list.sort(Comparator.comparing(LocationWeatherSnapshot::getTime));

        // 집계 버킷
        float sumPcp = 0f;
        float actualMax = Float.NEGATIVE_INFINITY, actualMin = Float.POSITIVE_INFINITY;
        float feelMax = Float.NEGATIVE_INFINITY, feelMin = Float.POSITIVE_INFINITY;

        // 시간별 플래그 & 가중치
        boolean[] rainCand = new boolean[24];
        boolean[] snowCand = new boolean[24];
        boolean[] windyCand = new boolean[24];
        int[] skyCode = new int[24]; // 0이면 없음
        float[] weight = new float[24];

        for (LocationWeatherSnapshot s : list) {
            int hh = s.getTime() / 100;
            if (hh < 0 || hh > 23) continue;

            float w = (hh >= DAY_START && hh < DAY_END) ? 1.2f : 0.8f;
            weight[hh] = w;

            Float tmp = s.getTmp();
            if (tmp != null) {
                actualMax = Math.max(actualMax, tmp);
                actualMin = Math.min(actualMin, tmp);
            }
            Float ft = s.getFeelTmp();
            if (ft != null) {
                feelMax = Math.max(feelMax, ft);
                feelMin = Math.min(feelMin, ft);
            }

            Float pop = nz(s.getPop());
            Integer pty = s.getPty();
            Integer sky = s.getSky();
            Float wsd = nz(s.getWsd());
            Float pcp = nz(s.getPcp());

            sumPcp += pcp;

            boolean isRain = (pty != null && (pty == 1 || pty == 4 || pty == 5)) || (pty != null && pty == 0 && pop >= POP_RAIN);
            boolean isSnow = (pty != null && (pty == 2 || pty == 3 || pty == 6 || pty == 7));
            boolean isWindy = (pty != null && pty == 0 && pop < POP_RAIN && wsd >= WINDY_MS);

            rainCand[hh] = isRain;
            snowCand[hh] = isSnow;
            windyCand[hh] = isWindy;

            // 강수/강풍 아닌 시간만 sky 집계 후보
            if (!isRain && !isSnow && !isWindy && sky != null) {
                // SKY: 1=맑음, 3=구름많음, 4=흐림
                if (sky == 1 || sky == 3 || sky == 4) skyCode[hh] = sky;
            }
        }

        // A+B 혼합: 연속조건 적용 후 '유효 가중시간' 계산
        float rainEff = effectiveWeightedHours(rainCand, weight);
        float snowEff = effectiveWeightedHours(snowCand, weight);
        float windyEff = effectiveWeightedHours(windyCand, weight);

        // 태그 결정 (우선순위: 눈 > 비 > 바람 > 하늘)
        WeatherTag chosenWeather;
        if (snowEff >= RAIN_SNOW_MIN_EFFECTIVE_HOURS) {
            chosenWeather = wtag.get("눈 오는 날");
        } else if (rainEff >= RAIN_SNOW_MIN_EFFECTIVE_HOURS && sumPcp >= RAIN_MIN_PCP_MM) {
            chosenWeather = wtag.get("비 오는 날");
        } else if (windyEff >= WINDY_MIN_EFFECTIVE_HOURS) {
            chosenWeather = wtag.get("바람 많은 날");
        } else {
            // SKY 가중 점유율
            float wClear = 0, wPCloudy = 0, wCloudy = 0, wTotal = 0;
            for (int h = 0; h < 24; h++) {
                if (skyCode[h] == 0) continue;
                wTotal += weight[h];
                if (skyCode[h] == 1) wClear += weight[h];
                else if (skyCode[h] == 3) wPCloudy += weight[h];
                else if (skyCode[h] == 4) wCloudy += weight[h];
            }
            float rClear = wTotal > 0 ? wClear / wTotal : 0;
            float rPCloudy = wTotal > 0 ? wPCloudy / wTotal : 0;
            float rCloudy = wTotal > 0 ? wCloudy / wTotal : 0;

            if (rClear >= 0.5f || (rClear >= rPCloudy && rClear >= rCloudy))
                chosenWeather = wtag.get("맑은 날");
            else if (rPCloudy >= 0.5f || (rPCloudy >= rClear && rPCloudy >= rCloudy))
                chosenWeather = wtag.get("구름 많은 날");
            else
                chosenWeather = wtag.get("흐린 날");
        }

        // 온도 요약
        Float feelsLikeAvgDay = averageFeelDaytime(list);
        TemperatureTag chosenTempTag = mapTempTag(feelsLikeAvgDay, ttags);

        SeasonTag chosenSeason = mapSeasonTag(date.getMonthValue(), stag);

        DailySummary ds = DailySummary.builder()
                .location(loc)
                .reportDate(date)
                .seasonTag(chosenSeason)
                .weatherTag(chosenWeather)
                .temperatureTag(chosenTempTag)
                .feelsLikeTmx(finiteOrNull(feelMax))
                .feelsLikeTmn(finiteOrNull(feelMin))
                .actualTmx(finiteOrNull(actualMax))
                .actualTmn(finiteOrNull(actualMin))
                .build();

        return ds;
    }

    /** 연속조건(A+B) 반영 후 유효 '가중' 시간 합 */
    private float effectiveWeightedHours(boolean[] cand, float[] w) {
        float sum = 0f;
        int run = 0;
        for (int h = 0; h <= 24; h++) {
            boolean on = (h < 24) && cand[h];
            if (on) run++;
            if (!on || h == 24) {
                if (run > 0) {
                    // 해당 구간이 주간/야간 혼재 가능 → 시간별 문턱 적용
                    // 시간별로 다시 보면서, 주간2/야간3 연속 충족한 시간만 가중 합산
                    for (int s = h - run; s < h; s++) {
                        int dayNeed = (s >= DAY_START && s < DAY_END) ? 2 : 3;
                        int len = contiguousLenAt(cand, s);
                        if (len >= dayNeed) sum += w[s];
                    }
                    run = 0;
                }
            }
        }
        return sum;
    }

    private int contiguousLenAt(boolean[] a, int idx) {
        int L = 0;
        for (int i = idx; i < a.length && a[i]; i++) L++;
        for (int i = idx - 1; i >= 0 && a[i]; i--) L++;
        return L;
    }

    private Float averageFeelDaytime(List<LocationWeatherSnapshot> list) {
        float sum = 0f, wsum = 0f;
        for (LocationWeatherSnapshot s : list) {
            int hh = s.getTime() / 100;
            if (hh < DAY_START || hh >= DAY_END) continue;
            if (s.getFeelTmp() == null) continue;
            sum += s.getFeelTmp();
            wsum += 1f;
        }
        if (wsum == 0f) {
            // 24h 평균 fallback
            for (LocationWeatherSnapshot s : list) {
                if (s.getFeelTmp() == null) continue;
                sum += s.getFeelTmp();
                wsum += 1f;
            }
        }
        return wsum == 0f ? null : (sum / wsum);
    }

    private TemperatureTag mapTempTag(Float avgFeel, List<TemperatureTag> ttags) {
        if (avgFeel == null) return null;
        for (TemperatureTag t : ttags) {
            float min = t.getMinTemperature();
            float max = t.getMaxTemperature();
            // 하한 포함, 상한 미포함(마지막 구간은 max=+∞처럼 등록되어 있어도 OK)
            if (avgFeel >= min && avgFeel < max) return t;
        }
        // 경계값 처리
        return ttags.stream().min(Comparator.comparing(TemperatureTag::getMinTemperature)).orElse(null);
    }

    private SeasonTag mapSeasonTag(int month, Map<String, SeasonTag> stag) {
        return switch (month) {
            case 3,4,5 -> stag.get("봄");
            case 6,7,8 -> stag.get("여름");
            case 9,10,11 -> stag.get("가을");
            default -> stag.get("겨울");
        };
    }

    private static int toInt(LocalDate d) { return Integer.parseInt(d.format(DateTimeFormatter.BASIC_ISO_DATE)); }
    private static Float nz(Float f) { return f == null ? 0f : f; }
    private static Float finiteOrNull(float v) {
        if (Float.isInfinite(v) || Float.isNaN(v)) return null;
        return v;
    }
}
