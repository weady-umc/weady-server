package com.weady.weady.domain.weather.service;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import com.weady.weady.domain.weather.entity.SkyCode;
import com.weady.weady.domain.weather.repository.LocationWeatherSnapshotRepository;
import com.weady.weady.domain.weather.repository.WeatherShortDetailRepository;
import jakarta.persistence.EntityManager;                                // [NEW] flush/clear용
import jakarta.persistence.PersistenceContext;                          // [NEW]
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    private static final int CHUNK_SIZE = 800;                          // (기존 유지)
    private static final int SAVE_BATCH_SIZE = 1000;                    // [NEW] saveAll 소배치 크기
    private static final int HOURS_PER_DAY = 24;
    private static final int DELETE_BATCH_LIMIT = 20000;                // [NEW] LIMIT 배치 삭제 크기

    private final LocationRepository locationRepository;
    private final WeatherShortDetailRepository detailRepository;
    private final LocationWeatherSnapshotRepository snapshotRepository;
    private final TransactionTemplate tx;                               // (기존 유지) txManager 필드 제거됨  // [CHANGED]

    @PersistenceContext
    private EntityManager em;                                           // [NEW] flush()/clear() 위해 주입

    /** 매일 23:25: 지난 스냅샷 정리 + 내일(00~23) 스냅샷 업서트 */
    public void buildNextDaySnapshots() {
        long start = System.currentTimeMillis();
        LocalDate todayKst = LocalDate.now(KST);
        int today = Integer.parseInt(todayKst.format(DateTimeFormatter.BASIC_ISO_DATE));
        int tomorrow = Integer.parseInt(todayKst.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

        // int deleted = tx.execute(status -> snapshotRepository.deleteOlderThan(today));
        // log.info("스냅샷 정리: {}건 삭제(date < {})", deleted, today);
        int totalDeleted = purgeOldSnapshotsInBatches(today);           // [CHANGED] LIMIT 배치 삭제로 교체
        log.info("스냅샷 정리(배치): {}건 삭제(date < {})", totalDeleted, today);  // [CHANGED]

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

            long t0 = System.currentTimeMillis();
            List<LocationWeatherSnapshot> existing = snapshotRepository.findByLocationIdsAndDate(chunk, tomorrow);
            Map<String, LocationWeatherSnapshot> existingMap = existing.stream()
                    .collect(Collectors.toMap(s -> key(s.getLocation().getId(), s.getDate(), s.getTime()), s -> s));

            List<LocationWeatherShortDetail> details = detailRepository.findByLocationIdsAndDate(chunk, tomorrow);
            long t1 = System.currentTimeMillis();

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
            long t2 = System.currentTimeMillis();

            // 기존: saveAll(toInsert), saveAll(toUpdate)를 큰 덩어리로 저장
            // → 변경: 소배치로 쪼개서 flush/clear 수행
            int ins = 0, upd = 0;
            if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
                int[] res = persistBatches(toInsert, toUpdate);         // [CHANGED] 소배치 저장
                ins = res[0];
                upd = res[1];
            }
            long t3 = System.currentTimeMillis();

            totalInserts += ins;
            totalUpdates += upd;
            processed += chunk.size();

            if (processed % 1000 == 0 || processed == total) {
                log.info("스냅샷 진행 {}/{}  select={}ms, build={}ms, save={}ms  (이번: insert {}, update {} / 누적: insert {}, update {})",
                        processed, total, (t1 - t0), (t2 - t1), (t3 - t2),
                        ins, upd, totalInserts, totalUpdates);
            }
        }

        long end = System.currentTimeMillis();
        log.info("스냅샷 완료. 대상 Location {}개, INSERT {}, UPDATE {}, 소요 {}ms",
                total, totalInserts, totalUpdates, (end - start));
    }

    // [NEW] LIMIT로 잘라 배치 삭제 (대량 데이터에서도 OOM/락 부담 완화)
    private int purgeOldSnapshotsInBatches(int cutoffDate) {
        int deletedTotal = 0;
        while (true) {
            int deleted = tx.execute(status -> snapshotRepository.deleteOlderThanLimit(cutoffDate, DELETE_BATCH_LIMIT));
            deletedTotal += deleted;
            if (deleted < DELETE_BATCH_LIMIT) break;
        }
        return deletedTotal;
    }

    // [NEW] saveAll을 소배치로 실행하면서 flush/clear로 1차캐시/메모리 압박 완화
    private int[] persistBatches(List<LocationWeatherSnapshot> toInsert, List<LocationWeatherSnapshot> toUpdate) {
        int inserted = 0;
        int updated = 0;

        if (!toInsert.isEmpty()) {
            inserted = tx.execute(status -> {
                int cnt = 0;
                for (int i = 0; i < toInsert.size(); i += SAVE_BATCH_SIZE) {
                    List<LocationWeatherSnapshot> part = toInsert.subList(i, Math.min(i + SAVE_BATCH_SIZE, toInsert.size()));
                    snapshotRepository.saveAll(part);
                    em.flush();
                    em.clear();
                    cnt += part.size();
                }
                return cnt;
            });
        }

        if (!toUpdate.isEmpty()) {
            updated = tx.execute(status -> {
                int cnt = 0;
                for (int i = 0; i < toUpdate.size(); i += SAVE_BATCH_SIZE) {
                    List<LocationWeatherSnapshot> part = toUpdate.subList(i, Math.min(i + SAVE_BATCH_SIZE, toUpdate.size()));
                    snapshotRepository.saveAll(part);
                    em.flush();
                    em.clear();
                    cnt += part.size();
                }
                return cnt;
            });
        }
        return new int[]{inserted, updated};
    }

    private static String key(Long locId, Integer date, Integer time) {
        return locId + "-" + date + "-" + time;
    }

    private static Integer mapPtyToInt(SkyCode sc) {
        if (sc == null) return 0;
        switch (sc) {
            case RAIN: return 1;
            case SNOW: return 2;
            default: return 0;
        }
    }

    private static Integer mapSkyToInt(SkyCode sc) {
        if (sc == null) return null;
        if (sc == SkyCode.RAIN || sc == SkyCode.SNOW) return 4;
        switch (sc) {
            case CLEAR: return 1;
            case PARTLY_CLOUDY: return 3;
            case CLOUDY: return 4;
            default: return 4;
        }
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
