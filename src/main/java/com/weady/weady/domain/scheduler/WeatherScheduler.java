package com.weady.weady.domain.scheduler;

import com.weady.weady.domain.scheduler.service.WeatherUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherUpdateService weatherUpdateService;

    // 매일 02:15, 05:15, 08:15, 11:15, 14:15, 17:15, 20:15, 23:15
    @Scheduled(cron = "0 17 2,5,8,11,14,17,20,23 * * *", zone = "Asia/Seoul")
    public void updateShortTermWeather() {
        log.info("[WeatherScheduler] 단기예보 업데이트 시작");
        try {
            weatherUpdateService.updateShortTermWeather();
        } catch (Exception e) {
            log.error("[WeatherScheduler] 단기예보 업데이트 중 예외 발생", e);
        }
        log.info("[WeatherScheduler] 단기예보 업데이트 종료");
    }
}
