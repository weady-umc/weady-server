package com.weady.weady.domain.fashion.service;

import com.weady.weady.domain.fashion.dto.Response.FashionSummaryResponseDto;
import com.weady.weady.domain.fashion.entity.Fashion;
import com.weady.weady.domain.fashion.repository.FashionRepository;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import com.weady.weady.domain.weather.repository.LocationWeatherSnapshotRepository;
import com.weady.weady.global.common.error.errorCode.FashionErrorCode;
import com.weady.weady.global.common.error.errorCode.UserErrorCode;
import com.weady.weady.global.common.error.errorCode.WeatherErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import com.weady.weady.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FashionService {
    private final FashionRepository fashionRepository;
    private final LocationWeatherSnapshotRepository locationWeatherSnapshotRepository;
    private final UserRepository userRepository;

    /**
     * 홈 화면에서 옷차림 요약 정보를 조회
     * @return FashionSummaryResponseDto
     * @thorws UserErrorCode.USER_NOT_FOUND 사용자가 존재하지 않을 경우 예외를 발생
     * @thorws WeatherErrorCode.WEATHER_SNAPSHOT_NOT_FOUND 날씨 snapshot이 존재하지 않을 경우 예외를 발생
     * @thorws FashionErrorCode.FASHION_NOT_FOUND 체감온도에 따른 옷차림 추천이 존재하지 않을 경우 예외를 발생
     */
    public FashionSummaryResponseDto getFashionSummary() {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Long locationId = null;
        if (user.getDefaultLocation() != null && user.getDefaultLocation().getLocation() != null) {
            locationId = user.getDefaultLocation().getLocation().getId();
        } else if (user.getNowLocation() != null) {
            locationId = user.getNowLocation().getId();
        }

        LocalDateTime today = LocalDateTime.now();
        int date = Integer.parseInt(today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        int time = today.getHour() * 100;

        LocationWeatherSnapshot snapshot = locationWeatherSnapshotRepository
                .findByLocationIdAndDateAndTime(locationId, date, time)
                .orElseThrow(() -> new BusinessException(WeatherErrorCode.WEATHER_SNAPSHOT_NOT_FOUND));

        float feelTemp = snapshot.getFeelTmp();

        Fashion fashion = fashionRepository
                .findByTemperatureRange(feelTemp)
                .orElseThrow(() -> new BusinessException(FashionErrorCode.FASHION_NOT_FOUND));

        return new FashionSummaryResponseDto(
                locationId,
                fashion.getName(),
                fashion.getImgUrl()
        );
    }
}
