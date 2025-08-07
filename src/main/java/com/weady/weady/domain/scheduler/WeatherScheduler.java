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

    /**
     * 단기예보 스케쥴러
     * 매일 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10 실행
     * 기상청 API가 보통 10분~15분 뒤에 데이터를 제공하므로 10분에 실행
     */
    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *")
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