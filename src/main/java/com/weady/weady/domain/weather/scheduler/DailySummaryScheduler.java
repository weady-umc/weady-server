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

    @Scheduled(cron = "0 45 23 * * *", zone = "Asia/Seoul")
    public void run() {

        ZoneId zone = java.time.ZoneId.of("Asia/Seoul");
        LocalTime DELAY_CUTOFF = LocalTime.of(1, 0); // 자정~01:00 사이 지연 허용
        ZonedDateTime now = ZonedDateTime.now(zone);
        LocalDate baseDay = now.toLocalDate();

        if (now.toLocalTime().isBefore(DELAY_CUTOFF)) {
            baseDay = baseDay.minusDays(1);
        }
        LocalDate targetDate = baseDay.plusDays(1);

        // 원래라면 23시 45분에 실행되어야 하는데 밀려서 00시 이후에 실행될 경우, 어제 날짜로 실행
        log.info("[DailySummaryScheduler] {} 요약 생성 시작 (now={}, baseDay={}, targetDate={})", targetDate, now, baseDay, targetDate);
        try {
            service.buildDailySummary(targetDate);
        } catch (Exception e) {
            log.error("[DailySummaryScheduler] 예외", e);
        }
        log.info("[DailySummaryScheduler] 종료");
    }
}