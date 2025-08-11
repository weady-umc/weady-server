package com.weady.weady.domain.scheduler;

import com.weady.weady.domain.scheduler.service.DailySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailySummaryScheduler {

    private final DailySummaryService service;

    @Scheduled(cron = "0 45 23 * * *", zone = "Asia/Seoul")
    public void run() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("[DailySummaryScheduler] {} 요약 생성 시작", tomorrow);
        try {
            service.buildDailySummary(tomorrow);
        } catch (Exception e) {
            log.error("[DailySummaryScheduler] 예외", e);
        }
        log.info("[DailySummaryScheduler] 종료");
    }
}