package com.weady.weady.domain.weather.scheduler;

import com.weady.weady.domain.weather.service.MidWeatherSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MidWeatherScheduler {
    private final MidWeatherSchedulerService service;

    @Async("weatherTaskExecutor")
    @Scheduled(cron = "0 15 6,18 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Seoul")
    public void run() {
        log.info("[MidWeatherScheduler] 중기예보 업데이트 시작");
        try {
            service.update();
        } catch (Exception e) {
            log.error("[MidWeatherScheduler] 예외", e);
        }
        log.info("[MidWeatherScheduler] 종료");
    }
}
