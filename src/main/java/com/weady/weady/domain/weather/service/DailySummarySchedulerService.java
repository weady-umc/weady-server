package com.weady.weady.domain.weather.service;

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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailySummarySchedulerService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final int DAY_START = 9, DAY_END = 21;
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

    public void buildForSchedulerWindow() {
        LocalTime cutoff = LocalTime.of(1, 0);
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(KST);
        LocalDate baseDay = now.toLocalTime().isBefore(cutoff)
                ? now.toLocalDate().minusDays(1)
                : now.toLocalDate();
        LocalDate target = baseDay.plusDays(1);
        buildDailySummary(target);
    }

    public void buildForToday() {
        buildDailySummary(LocalDate.now(KST));
    }

    public void buildDailySummary(LocalDate date) {
        long t0 = System.currentTimeMillis();

        LocalDate todayKst = LocalDate.now(KST);
        int purged = tx.execute(status -> summaryRepo.deleteByReportDateBefore(todayKst));
        log.info("DailySummary purged: {}", purged);

        List<Location> allLocations = locationRepo.findAll();
        if (allLocations.isEmpty()) return;

        Map<Long, List<Location>> gridToLocations = allLocations.stream()
                .filter(l -> l.getGridLocation() != null)
                .collect(Collectors.groupingBy(l -> l.getGridLocation().getId()));

        List<Long> allGridIds = new ArrayList<>(gridToLocations.keySet());

        Map<String, WeatherTag> wtag = weatherTagRepo.findAll().stream()
                .collect(Collectors.toMap(WeatherTag::getName, x -> x));
        List<TemperatureTag> ttags = tempTagRepo.findAll();
        Map<String, SeasonTag> stag = seasonTagRepo.findAll().stream()
                .collect(Collectors.toMap(SeasonTag::getName, x -> x));

        final int CHUNK = 800;
        int totalGrids = allGridIds.size();
        int processedGrids = 0, ins = 0, upd = 0;

        for (int i = 0; i < totalGrids; i += CHUNK) {
            List<Long> chunkGridIds = allGridIds.subList(i, Math.min(i + CHUNK, totalGrids));

            List<LocationWeatherSnapshot> snaps = snapRepo.findByGridLocationIdInAndDate(chunkGridIds, toInt(date));

            Map<Long, List<LocationWeatherSnapshot>> byGrid = snaps.stream()
                    .collect(Collectors.groupingBy(s -> s.getGridLocation().getId()));

            for (Long gridId : chunkGridIds) {
                List<LocationWeatherSnapshot> day = byGrid.getOrDefault(gridId, List.of());
                if (day.isEmpty()) continue;

                DailySummary baseDs = calculateSummary(date, day, wtag, ttags, stag);
                if (baseDs == null) continue;

                List<Location> targetLocations = gridToLocations.get(gridId);
                if (targetLocations == null) continue;

                for (Location loc : targetLocations) {
                    int changed = tx.execute(status -> {
                        DailySummary old = summaryRepo.findByLocationIdAndReportDate(loc.getId(), date).orElse(null);
                        if (old == null) {
                            DailySummary newDs = cloneSummaryForLocation(baseDs, loc);
                            summaryRepo.save(newDs);
                            return 1;
                        }
                        old.setSeasonTag(baseDs.getSeasonTag());
                        old.setWeatherTag(baseDs.getWeatherTag());
                        old.setTemperatureTag(baseDs.getTemperatureTag());
                        old.setFeelsLikeTmx(baseDs.getFeelsLikeTmx());
                        old.setFeelsLikeTmn(baseDs.getFeelsLikeTmn());
                        old.setActualTmx(baseDs.getActualTmx());
                        old.setActualTmn(baseDs.getActualTmn());
                        return 2;
                    });
                    if (changed == 1) ins++; else if (changed == 2) upd++;
                }
            }
            processedGrids += chunkGridIds.size();
            log.info("DailySummary Grid Progress {}/{}", processedGrids, totalGrids);
        }
        log.info("DailySummary Completed: insert={}, update={}, time={}ms", ins, upd, (System.currentTimeMillis() - t0));
    }

    private DailySummary calculateSummary(
            LocalDate date, List<LocationWeatherSnapshot> list,
            Map<String, WeatherTag> wtag, List<TemperatureTag> ttags, Map<String, SeasonTag> stag) {

        if (list.isEmpty()) return null;

        list.sort(Comparator.comparing(LocationWeatherSnapshot::getTime));

        float sumPcp = 0f;
        float actualMax = Float.NEGATIVE_INFINITY, actualMin = Float.POSITIVE_INFINITY;
        float feelMax = Float.NEGATIVE_INFINITY, feelMin = Float.POSITIVE_INFINITY;

        boolean[] rainCand = new boolean[24];
        boolean[] snowCand = new boolean[24];
        boolean[] windyCand = new boolean[24];
        int[] skyCode = new int[24];
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

            if (!isRain && !isSnow && !isWindy && sky != null) {
                if (sky == 1 || sky == 3 || sky == 4) skyCode[hh] = sky;
            }
        }

        float rainEff = effectiveWeightedHours(rainCand, weight);
        float snowEff = effectiveWeightedHours(snowCand, weight);
        float windyEff = effectiveWeightedHours(windyCand, weight);

        WeatherTag chosenWeather;
        if (snowEff >= RAIN_SNOW_MIN_EFFECTIVE_HOURS) {
            chosenWeather = wtag.get("눈 오는 날");
        } else if (rainEff >= RAIN_SNOW_MIN_EFFECTIVE_HOURS && sumPcp >= RAIN_MIN_PCP_MM) {
            chosenWeather = wtag.get("비 오는 날");
        } else if (windyEff >= WINDY_MIN_EFFECTIVE_HOURS) {
            chosenWeather = wtag.get("바람 많은 날");
        } else {
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

        Float feelsLikeAvgDay = averageFeelDaytime(list);
        TemperatureTag chosenTempTag = mapTempTag(feelsLikeAvgDay, ttags);
        SeasonTag chosenSeason = mapSeasonTag(date.getMonthValue(), stag);

        return DailySummary.builder()
                .reportDate(date)
                .seasonTag(chosenSeason)
                .weatherTag(chosenWeather)
                .temperatureTag(chosenTempTag)
                .feelsLikeTmx(finiteOrNull(feelMax))
                .feelsLikeTmn(finiteOrNull(feelMin))
                .actualTmx(finiteOrNull(actualMax))
                .actualTmn(finiteOrNull(actualMin))
                .build();
    }

    private DailySummary cloneSummaryForLocation(DailySummary base, Location loc) {
        return DailySummary.builder()
                .location(loc)
                .reportDate(base.getReportDate())
                .seasonTag(base.getSeasonTag())
                .weatherTag(base.getWeatherTag())
                .temperatureTag(base.getTemperatureTag())
                .feelsLikeTmx(base.getFeelsLikeTmx())
                .feelsLikeTmn(base.getFeelsLikeTmn())
                .actualTmx(base.getActualTmx())
                .actualTmn(base.getActualTmn())
                .build();
    }

    private float effectiveWeightedHours(boolean[] cand, float[] w) {
        float sum = 0f;
        int run = 0;
        for (int h = 0; h <= 24; h++) {
            boolean on = (h < 24) && cand[h];
            if (on) run++;
            if (!on || h == 24) {
                if (run > 0) {
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
            if (avgFeel >= min && avgFeel < max) return t;
        }
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