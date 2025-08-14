package com.weady.weady.domain.weather.scheduler;

import com.weady.weady.domain.weather.service.ShortWeatherSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final ShortWeatherSchedulerService shortWeatherSchedulerService;

    // 매일 02:5, 05:5, 11:5, 17:5, 23:5 에 단기예보 업데이트
    @Scheduled(cron = "0 5 5,11,17,23 * * *", zone = "Asia/Seoul")
    public void updateShortTermWeather() {
        log.info("[WeatherScheduler] 단기예보 업데이트 시작");
        try {
            shortWeatherSchedulerService.updateShortTermWeather();
        } catch (Exception e) {
            log.error("[WeatherScheduler] 단기예보 업데이트 중 예외 발생", e);
        }
        log.info("[WeatherScheduler] 단기예보 업데이트 종료");
    }
}
