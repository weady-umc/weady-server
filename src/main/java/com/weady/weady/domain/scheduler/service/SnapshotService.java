package com.weady.weady.domain.scheduler.service;

import org.springframework.stereotype.Service;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import com.weady.weady.domain.weather.entity.SkyCode;
import com.weady.weady.domain.weather.repository.LocationWeatherSnapshotRepository;
import com.weady.weady.domain.weather.repository.WeatherShortDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SnapshotService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int CHUNK_SIZE = 800;   // location id 청크 크기
    private static final int HOURS_PER_DAY = 24;

    private final LocationRepository locationRepository;
    private final WeatherShortDetailRepository detailRepository;
    private final LocationWeatherSnapshotRepository snapshotRepository;
    private final TransactionTemplate tx;

    public SnapshotService(LocationRepository locationRepository,
                           WeatherShortDetailRepository detailRepository,
                           LocationWeatherSnapshotRepository snapshotRepository,
                           PlatformTransactionManager txManager) {
        this.locationRepository = locationRepository;
        this.detailRepository = detailRepository;
        this.snapshotRepository = snapshotRepository;
        this.tx = new TransactionTemplate(txManager);
    }

    // 매일 23:40에 실행: 지난 스냅샷 삭제 + 내일(00~23시) 스냅샷 업서트
    public void buildNextDaySnapshots() {
        long start = System.currentTimeMillis();
        LocalDate todayKst = LocalDate.now(KST);
        int today = Integer.parseInt(todayKst.format(DateTimeFormatter.BASIC_ISO_DATE));
        int tomorrow = Integer.parseInt(todayKst.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

        // 지난 날짜 스냅샷 정리
        int deleted = tx.execute(status -> snapshotRepository.deleteOlderThan(today));
        log.info("스냅샷 정리: {}건 삭제(date < {})", deleted, today);

        // 전체 Location 을 청크로 나눠 처리
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

            // 이미 존재하는 내일자 스냅샷 읽기 (업서트 대비)
            List<LocationWeatherSnapshot> existing = snapshotRepository.findByLocationIdsAndDate(chunk, tomorrow);
            Map<String, LocationWeatherSnapshot> existingMap = existing.stream()
                    .collect(Collectors.toMap(s -> key(s.getLocation().getId(), s.getDate(), s.getTime()), s -> s));

            // 내일자 예보(00~23시) 디테일 읽기
            List<LocationWeatherShortDetail> details = detailRepository.findByLocationIdsAndDate(chunk, tomorrow);

            // 디테일 -> 스냅샷 변환 후 업서트 준비
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


    /* ---------- 매핑/유틸 ---------- */
    private static String key(Long locId, Integer date, Integer time) {
        return locId + "-" + date + "-" + time;
    }

    // PTY 정수: 없음=0, 비=1, 눈=2
    private static Integer mapPtyToInt(SkyCode sc) {
        if (sc == null) return 0;
        return switch (sc) {
            case RAIN -> 1;
            case SNOW -> 2;
            default -> 0;
        };
    }

    // SKY 정수: 강수(PTY≠0)면 무조건 4, 아니면 CLEAR=1, PARTLY_CLOUDY=3, CLOUDY=4
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

    // ===== 체감온도 계산 (Heat Index / Wind Chill / Apparent Temperature) =====
    private static Float deriveFeelsLike(LocationWeatherShortDetail d) {
        Float t = d.getTmp();   // °C
        Float rh = d.getReh();  // %
        Float ws = d.getWsd();  // m/s

        if (t == null) return null;

        // Hot: 더울 때의 체감온도 (T>=27°C & RH>=40%)
        if (rh != null && t >= 27f && rh >= 40f) {
            return round1(heatIndexC(t, rh));
        }

        // Cold: 추울 때의 체감온도 (T<=10°C & wind > 1.34 m/s)
        if (ws != null && t <= 10f && ws > 1.34f) {
            return round1(windChillC(t, ws));
        }

        // 일반적인 체감온도 (T, RH, WS 모두 사용 가능)
        if (rh != null && ws != null) {
            return round1(apparentTemperatureC(t, rh, ws));
        }
        if (ws != null) {
            return round1(t - 0.7f * ws); // 바람만 있을 때 간단 보정
        }
        return round1(t);
    }

    // Apparent Temperature (BoM)
    private static float apparentTemperatureC(float tC, float rhPct, float wsMs) {
        double e = (rhPct / 100.0) * 6.105 * Math.exp((17.27 * tC) / (237.7 + tC));
        return (float) (tC + 0.33 * e - 0.70 * wsMs - 4.0);
    }

    // Heat Index (NOAA): °C in/out, 내부는 화씨 공식 사용
    private static float heatIndexC(float tC, float rhPct) {
        double tF = cToF(tC);

        double hiF =
                -42.379 + 2.04901523*tF + 10.14333127* (double) rhPct
                        - 0.22475541*tF* (double) rhPct - 0.00683783*tF*tF - 0.05481717* (double) rhPct * (double) rhPct
                        + 0.00122874*tF*tF* (double) rhPct + 0.00085282*tF* (double) rhPct * (double) rhPct
                        - 0.00000199*tF*tF* (double) rhPct * (double) rhPct;

        if ((double) rhPct < 13 && tF >= 80 && tF <= 112) {
            hiF -= ((13 - (double) rhPct) / 4.0) * Math.sqrt((17 - Math.abs(tF - 95.0)) / 17.0);
        } else if ((double) rhPct > 85 && tF >= 80 && tF <= 87) {
            hiF += (((double) rhPct - 85) / 10.0) * ((87 - tF) / 5.0);
        }

        double hiC = fToC(hiF);
        return (float) Math.max(hiC, tC);
    }

    // Wind Chill (Canada/NWS): T °C, V km/h (ws m/s 입력)
    private static float windChillC(float tC, float wsMs) {
        double vKmh = wsMs * 3.6;
        double twc = 13.12 + 0.6215*tC - 11.37*Math.pow(vKmh, 0.16)
                + 0.3965*tC*Math.pow(vKmh, 0.16);
        return (float) Math.min(tC, twc); // 체감이 원온도보다 높아지지 않도록
    }

    private static double cToF(double c) { return c * 9.0/5.0 + 32.0; }
    private static double fToC(double f) { return (f - 32.0) * 5.0/9.0; }
    private static Float round1(float v) { return Math.round(v * 10f) / 10f; }
}