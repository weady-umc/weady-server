package com.weady.weady.domain.scheduler;

import com.weady.weady.domain.scheduler.service.MidWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@Slf4j
@RequiredArgsConstructor
public class MidWeatherScheduler {
    private final MidWeatherService service;

    @Scheduled(cron = "0 10 6,18 * * *", zone = "Asia/Seoul")
    public void run() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        log.info("[MidWeatherScheduler] {} 단기예보 업데이트 시작", tomorrow);
        try {
            //service.
        } catch (Exception e) {
            log.error("[DailySummaryScheduler] 예외", e);
        }
        log.info("[DailySummaryScheduler] 종료");
    }
}
