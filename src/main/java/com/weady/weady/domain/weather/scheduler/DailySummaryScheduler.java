package com.weady.weady.domain.weather.scheduler;

import com.weady.weady.domain.weather.service.DailySummarySchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailySummaryScheduler {

    private final DailySummarySchedulerService service;

    @Scheduled(cron = "0 50 16,23 * * *", zone = "Asia/Seoul")
    public void run() {
        log.info("[DailySummaryScheduler] buildForSchedulerWindow 시작");
        try {
//            service.buildForSchedulerWindow();
            service.buildForToday(); // 복구용 오늘날짜로 테스트
        } catch (Exception e) {
            log.error("[DailySummaryScheduler] 예외", e);
        }
        log.info("[DailySummaryScheduler] 종료");
    }
}