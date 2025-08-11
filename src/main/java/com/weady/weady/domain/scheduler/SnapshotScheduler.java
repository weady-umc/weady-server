package com.weady.weady.domain.scheduler;

import com.weady.weady.domain.scheduler.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotScheduler {

    private final SnapshotService snapshotService;

    // 매일 23:30 KST에 내일(00~23시) 스냅샷 생성 + 지난 날짜 스냅샷 정리
    @Scheduled(cron = "0 27 23 * * *", zone = "Asia/Seoul")
    public void buildNextDaySnapshots() {
        log.info("[SnapshotScheduler] 내일 스냅샷 생성 시작");
        try {
            snapshotService.buildNextDaySnapshots();
        } catch (Exception e) {
            log.error("[SnapshotScheduler] 스냅샷 생성 중 예외", e);
        }
        log.info("[SnapshotScheduler] 내일 스냅샷 생성 종료");
    }
}