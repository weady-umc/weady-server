package com.weady.weady.domain.weather.service;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import com.weady.weady.domain.weather.entity.SkyCode;
import com.weady.weady.domain.weather.repository.LocationWeatherSnapshotRepository;
import com.weady.weady.domain.weather.repository.WeatherShortDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotSchedulerService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CHUNK_SIZE = 800;
    private static final int HOURS_PER_DAY = 24;

    private final LocationRepository locationRepository;
    private final WeatherShortDetailRepository detailRepository;
    private final LocationWeatherSnapshotRepository snapshotRepository;

    // Tx 구성
    private final PlatformTransactionManager txManager;
    private final TransactionTemplate tx;

    // 매일 23:40에 실행: 지난 스냅샷 삭제 + 내일(00~23시) 스냅샷 업서트
    public void buildNextDaySnapshots() {
        long start = System.currentTimeMillis();
        LocalDate todayKst = LocalDate.now(KST);
        int today = Integer.parseInt(todayKst.format(DateTimeFormatter.BASIC_ISO_DATE));
        int tomorrow = Integer.parseInt(todayKst.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

        int deleted = tx.execute(status -> snapshotRepository.deleteOlderThan(today));
        log.info("스냅샷 정리: {}건 삭제(date < {})", deleted, today);

        List<Long> allLocationIds = locationRepository.findAll().stream()
                .map(Location::getId)
                .toList();
        if (allLocationIds.isEmpty()) {
            log.warn("스냅샷 대상 Location이 없습니다.");
            return;
        }

        int total = allLocationIds.size();
        int processed = 0;
        int totalInserts = 0;
        int totalUpdates = 0;

        for (int i = 0; i < allLocationIds.size(); i += CHUNK_SIZE) {
            List<Long> chunk = allLocationIds.subList(i, Math.min(i + CHUNK_SIZE, total));

            List<LocationWeatherSnapshot> existing = snapshotRepository.findByLocationIdsAndDate(chunk, tomorrow);
            Map<String, LocationWeatherSnapshot> existingMap = existing.stream()
                    .collect(Collectors.toMap(s -> key(s.getLocation().getId(), s.getDate(), s.getTime()), s -> s));

            List<LocationWeatherShortDetail> details = detailRepository.findByLocationIdsAndDate(chunk, tomorrow);

            List<LocationWeatherSnapshot> toInsert = new ArrayList<>(details.size());
            List<LocationWeatherSnapshot> toUpdate = new ArrayList<>(Math.min(details.size(), existing.size()));

            for (LocationWeatherShortDetail d : details) {
                int hour = d.getTime() / 100;
                if (hour < 0 || hour >= HOURS_PER_DAY) continue;

                Integer pty = mapPtyToInt(d.getSkyCode());
                Integer sky = mapSkyToInt(d.getSkyCode());

                String k = key(d.getLocation().getId(), d.getDate(), d.getTime());
                LocationWeatherSnapshot snap = existingMap.get(k);
                if (snap == null) {
                    snap = LocationWeatherSnapshot.builder()
                            .location(d.getLocation())
                            .observationDate(d.getObservationDate())
                            .observationTime(d.getObservationTime())
                            .date(d.getDate())
                            .time(d.getTime())
                            .tmp(d.getTmp())
                            .feelTmp(deriveFeelsLike(d))
                            .sky(sky)
                            .wsd(d.getWsd())
                            .pty(pty)
                            .pop(d.getPop())
                            .pcp(d.getPcp())
                            .build();
                    toInsert.add(snap);
                } else {
                    snap.setObservationDate(d.getObservationDate());
                    snap.setObservationTime(d.getObservationTime());
                    snap.setTmp(d.getTmp());
                    snap.setFeelTmp(deriveFeelsLike(d));
                    snap.setSky(sky);
                    snap.setWsd(d.getWsd());
                    snap.setPty(pty);
                    snap.setPop(d.getPop());
                    snap.setPcp(d.getPcp());
                    toUpdate.add(snap);
                }
            }

            int ins = 0, upd = 0;
            if (!toInsert.isEmpty()) {
                ins = tx.execute(status -> snapshotRepository.saveAll(toInsert).size());
            }
            if (!toUpdate.isEmpty()) {
                upd = tx.execute(status -> snapshotRepository.saveAll(toUpdate).size());
            }

            totalInserts += ins;
            totalUpdates += upd;
            processed += chunk.size();

            if (processed % 1000 == 0 || processed == total) {
                log.info("스냅샷 진행 {}/{} (이번 청크: insert {}, update {} / 누적 insert {}, update {})",
                        processed, total, ins, upd, totalInserts, totalUpdates);
            }
        }

        long end = System.currentTimeMillis();
        log.info("스냅샷 완료. 대상 Location {}개, INSERT {}, UPDATE {}, 소요 {}ms",
                total, totalInserts, totalUpdates, (end - start));
    }

    private static String key(Long locId, Integer date, Integer time) {
        return locId + "-" + date + "-" + time;
    }

    private static Integer mapPtyToInt(SkyCode sc) {
        if (sc == null) return 0;
        return switch (sc) {
            case RAIN -> 1;
            case SNOW -> 2;
            default -> 0;
        };
    }

    private static Integer mapSkyToInt(SkyCode sc) {
        if (sc == null) return null;
        if (sc == SkyCode.RAIN || sc == SkyCode.SNOW) return 4;
        return switch (sc) {
            case CLEAR -> 1;
            case PARTLY_CLOUDY -> 3;
            case CLOUDY -> 4;
            default -> 4;
        };
    }

    private static Float deriveFeelsLike(LocationWeatherShortDetail d) {
        Float t = d.getTmp();
        Float rh = d.getReh();
        Float ws = d.getWsd();

        if (t == null) return null;

        if (rh != null && t >= 27f && rh >= 40f) {
            return round1(heatIndexC(t, rh));
        }
        if (ws != null && t <= 10f && ws > 1.34f) {
            return round1(windChillC(t, ws));
        }
        if (rh != null && ws != null) {
            return round1(apparentTemperatureC(t, rh, ws));
        }
        if (ws != null) {
            return round1(t - 0.7f * ws);
        }
        return round1(t);
    }

    private static float apparentTemperatureC(float tC, float rhPct, float wsMs) {
        double e = (rhPct / 100.0) * 6.105 * Math.exp((17.27 * tC) / (237.7 + tC));
        return (float) (tC + 0.33 * e - 0.70 * wsMs - 4.0);
    }

    private static float heatIndexC(float tC, float rhPct) {
        double tF = cToF(tC);
        double hiF =
                -42.379 + 2.04901523*tF + 10.14333127*rhPct
                        - 0.22475541*tF*rhPct - 0.00683783*tF*tF - 0.05481717*rhPct*rhPct
                        + 0.00122874*tF*tF*rhPct + 0.00085282*tF*rhPct*rhPct
                        - 0.00000199*tF*tF*rhPct*rhPct;

        if (rhPct < 13 && tF >= 80 && tF <= 112) {
            hiF -= ((13 - rhPct) / 4.0) * Math.sqrt((17 - Math.abs(tF - 95.0)) / 17.0);
        } else if (rhPct > 85 && tF >= 80 && tF <= 87) {
            hiF += ((rhPct - 85) / 10.0) * ((87 - tF) / 5.0);
        }
        double hiC = fToC(hiF);
        return (float) Math.max(hiC, tC);
    }

    private static float windChillC(float tC, float wsMs) {
        double vKmh = wsMs * 3.6;
        double twc = 13.12 + 0.6215*tC - 11.37*Math.pow(vKmh, 0.16)
                + 0.3965*tC*Math.pow(vKmh, 0.16);
        return (float) Math.min(tC, twc);
    }

    private static double cToF(double c) { return c * 9.0/5.0 + 32.0; }
    private static double fToC(double f) { return (f - 32.0) * 5.0/9.0; }
    private static Float round1(float v) { return Math.round(v * 10f) / 10f; }
}
