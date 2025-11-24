package com.weady.weady.domain.weather.service;

import com.weady.weady.common.error.errorCode.DailySummaryErrorCode;
import com.weady.weady.common.error.errorCode.LocationErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.errorCode.WeatherErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.external.kakao.KakaoRegionService;
import com.weady.weady.common.util.SecurityUtil;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.domain.weather.dto.response.GetLocationWeatherShortDetailResponse;
import com.weady.weady.domain.weather.dto.response.GetWeatherMidDetailResponse;
import com.weady.weady.domain.weather.dto.response.LocationTagResponseDto;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.WeatherMidDetail;
import com.weady.weady.domain.weather.mapper.WeatherMapper;
import com.weady.weady.domain.weather.repository.DailySummaryRepository;
import com.weady.weady.domain.weather.repository.WeatherMidDetailRepository;
import com.weady.weady.domain.weather.repository.WeatherShortDetailRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherShortDetailRepository weatherRepository;
    private final WeatherMidDetailRepository weatherMidDetailRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final LocationRepository locationRepository;
    private final KakaoRegionService kakaoRegionService;
    private final UserRepository userRepository;

    @Transactional
    public GetLocationWeatherShortDetailResponse getShortWeatherInfo() {
        Location locationToQuery = getLocationForWeatherQuery();
        return getWeatherInfoByLocation(locationToQuery);
    }

    @Transactional
    public GetLocationWeatherShortDetailResponse getWeatherPreview(String b_code, double longitude, double latitude) {
        String bCode = b_code;
        if (!StringUtils.hasText(bCode)) {
            bCode = kakaoRegionService.getBCodeByCoordinates(longitude, latitude);
        }

        Location location = locationRepository.findLocationBybCode(bCode).orElseThrow(() -> new BusinessException(LocationErrorCode.GRID_LOCATION_NOT_FOUND));

        return getWeatherInfoByLocation(location);
    }

    private GetLocationWeatherShortDetailResponse getWeatherInfoByLocation(Location location) {
        LocalDate today = LocalDate.now();
        DailySummary summary = dailySummaryRepository.findByLocationIdAndReportDate(location.getId(), today).orElseThrow(() -> new BusinessException(WeatherErrorCode.SUMMARY_DATA_NOT_FOUND));


        if (location.getGridLocation() == null) {
            throw new BusinessException(LocationErrorCode.GRID_LOCATION_NOT_FOUND);
        }
        Long gridLocationId = location.getGridLocation().getId();

        List<LocationWeatherShortDetail> allForecasts = weatherRepository.findByGridLocationIdOrderByDateAscTimeAsc(gridLocationId);

        if (allForecasts.isEmpty()) {
            throw new BusinessException(WeatherErrorCode.WEATHER_DATA_NOT_FOUND);
        }

        int todayInt = Integer.parseInt(today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        int currentTime = Integer.parseInt(LocalTime.now().format(DateTimeFormatter.ofPattern("HH00")));

        List<LocationWeatherShortDetail> filteredForecasts = allForecasts.stream()
                .filter(f -> f.getDate() > todayInt || (f.getDate().equals(todayInt) && f.getTime() >= currentTime))
                .collect(Collectors.toList());

        return WeatherMapper.toShortWeatherResponse(location, summary, filteredForecasts);
    }


    @Transactional
    public List<GetWeatherMidDetailResponse> getMidWeatherInfo() {

        //getLocationForWeatherQuery를 통해 쿼리에 필요한 Location 엔티티 가져옴
        Location locationToQuery = getLocationForWeatherQuery();

        String midTermRegCode = locationToQuery.getMidTermRegCode();

        //늘 날짜와 7일 후 날짜를 'YYYYMMDD' 포맷의 정수로 준비
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        //해당 지역의 1주일치 중기 예보 데이터를 조회
        List<WeatherMidDetail> forecastEntities = weatherMidDetailRepository
                .findByMidTermRegCodeAndDateBetweenOrderByDateAsc(midTermRegCode, today, endDate);

        //조회된 엔티티 리스트를 Stream을 사용하여 응답 DTO 리스트로 변환
        return forecastEntities.stream()
                .map(WeatherMapper::toMidWeatherResponse)
                .collect(Collectors.toList());
    }

    /**
     * 해당 위치의 daily_summary 계절, 기온, 날씨 태그 ID를 가져오는 로직입니다
     * @return LocationTagResponseDto
     */
    public LocationTagResponseDto getLocationTag() {

        User user = getAuthenticatedUser();

        Location nowLocation = user.getNowLocation();
        if (nowLocation == null)
            throw new BusinessException(LocationErrorCode.NOW_LOCATION_NOT_FOUND);

        LocalDate today = LocalDate.now();

        DailySummary dailySummary = dailySummaryRepository.findByLocationIdAndReportDateWithTags(nowLocation.getId(), today)
                .orElseThrow(() -> new BusinessException(DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND));

        return WeatherMapper.toLocationTagResponse(dailySummary);
    }



    private User getAuthenticatedUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private Location getLocationForWeatherQuery() {

        //User 엔티티 조회
        User user = getAuthenticatedUser();

        //기본위치 엔티티 조회
        UserFavoriteLocation defaultLocation = user.getDefaultLocation();

        //사용자 기본위치가 없다면 현재위치로 처리
        if (defaultLocation != null)
            return defaultLocation.getLocation();
        else {
            Location nowLocation = user.getNowLocation();
            if (nowLocation == null)
                throw new BusinessException(LocationErrorCode.NOW_LOCATION_NOT_FOUND);
            return nowLocation;
        }

    }

}
