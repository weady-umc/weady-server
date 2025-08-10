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
    private static final int CHUNK_SIZE = 800;   // location id 청크 크기 (메모리/DB 부하 균형)
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

    /** 매일 23:40에 실행: 지난 스냅샷 삭제 + 내일(00~23시) 스냅샷 업서트 */
    public void buildNextDaySnapshots() {
        long start = System.currentTimeMillis();

        // 1) KST 기준 날짜 계산
        LocalDate todayKst = LocalDate.now(KST);
        int today = Integer.parseInt(todayKst.format(DateTimeFormatter.BASIC_ISO_DATE));              // yyyyMMdd
        int tomorrow = Integer.parseInt(todayKst.plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

        // 2) 지난 날짜 스냅샷 정리
        int deleted = tx.execute(status -> snapshotRepository.deleteOlderThan(today));
        log.info("스냅샷 정리: {}건 삭제(date < {})", deleted, today);

        // 3) 전체 Location을 청크로 나눠 처리
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
                // 시간대 검증(00,01,...,23시) — 디테일이 더 많은 시간대 포함할 가능성 방어
                int hour = d.getTime() / 100;
                if (hour < 0 || hour >= HOURS_PER_DAY) continue;

                // Sky/Pty 정수 매핑
                Integer pty = mapPtyToInt(d.getSkyCode());  // 0/1/2
                Integer sky = mapSkyToInt(d.getSkyCode());  // 강수면 4, 아니면 1/3/4

                String k = key(d.getLocation().getId(), d.getDate(), d.getTime());
                LocationWeatherSnapshot snap = existingMap.get(k);
                if (snap == null) {
                    // 신규
                    snap = LocationWeatherSnapshot.builder()
                            .location(d.getLocation())
                            .observationDate(d.getObservationDate())
                            .observationTime(d.getObservationTime())
                            .date(d.getDate())   // 내일 yyyyMMdd
                            .time(d.getTime())   // HHmm
                            .feelTmp(deriveFeelsLike(d))   // 간단: tmp 기반 (추후 공식 적용 가능)
                            .sky(sky)                         // Integer
                            .wsd(d.getWsd())
                            .pty(pty)                         // Integer
                            .pop(d.getPop())
                            .build();
                    toInsert.add(snap);
                } else {
                    // 갱신
                    snap.setObservationDate(d.getObservationDate());
                    snap.setObservationTime(d.getObservationTime());
                    snap.setFeelTmp(deriveFeelsLike(d));
                    snap.setSky(sky);
                    snap.setWsd(d.getWsd());
                    snap.setPty(pty);
                    snap.setPop(d.getPop());
                    toUpdate.add(snap);
                }
            }

            // 배치 저장 (JPA batch 설정 활용)
            int ins = 0, upd = 0;
            if (!toInsert.isEmpty()) {
                int saved = tx.execute(status -> snapshotRepository.saveAll(toInsert).size());
                ins = saved;
            }
            if (!toUpdate.isEmpty()) {
                int saved = tx.execute(status -> snapshotRepository.saveAll(toUpdate).size());
                upd = saved;
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

    // 임시 체감온도: 현재는 TMP 그대로 사용 (추후 '열지수/풍속체감' 적용 가능)
    private static Float deriveFeelsLike(LocationWeatherShortDetail d) {
        return d.getTmp();
    }

    /** 스냅샷용 PTY 정수: 없음=0, 비=1, 눈=2  */
    private static Integer mapPtyToInt(SkyCode sc) {
        if (sc == null) return 0;
        return switch (sc) {
            case RAIN -> 1;
            case SNOW -> 2;
            default -> 0;  // CLEAR, PARTLY_CLOUDY, CLOUDY
        };
    }

    /** 스냅샷용 SKY 정수:
     *  - 강수(PTY≠0)면 무조건 4
     *  - 아니면 CLEAR=1, PARTLY_CLOUDY=3, CLOUDY=4
     */
    private static Integer mapSkyToInt(SkyCode sc) {
        if (sc == null) return null;
        if (sc == SkyCode.RAIN || sc == SkyCode.SNOW) return 4; // 강수시 고정
        return switch (sc) {
            case CLEAR -> 1;
            case PARTLY_CLOUDY -> 3;
            case CLOUDY -> 4;
            default -> 4;
        };
    }
}
