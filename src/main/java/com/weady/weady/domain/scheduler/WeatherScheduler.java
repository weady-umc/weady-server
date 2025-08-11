package com.weady.weady.domain.scheduler;

import com.weady.weady.domain.scheduler.service.ShortTermWeatherUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final ShortTermWeatherUpdateService shortTermWeatherUpdateService;

    // 매일 02:15, 05:10, 11:10, 17:10, 23:10 에 단기예보 업데이트
    @Scheduled(cron = "0 10 5,11,17,23 * * *", zone = "Asia/Seoul")
    public void updateShortTermWeather() {
        log.info("[WeatherScheduler] 단기예보 업데이트 시작");
        try {
            shortTermWeatherUpdateService.updateShortTermWeather();
        } catch (Exception e) {
            log.error("[WeatherScheduler] 단기예보 업데이트 중 예외 발생", e);
        }
        log.info("[WeatherScheduler] 단기예보 업데이트 종료");
    }
}
