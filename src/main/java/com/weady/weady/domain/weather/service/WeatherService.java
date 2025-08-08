package com.weady.weady.domain.weather.service;

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
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.WeatherMidDetail;
import com.weady.weady.domain.weather.mapper.WeatherMapper;
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
    private final LocationRepository locationRepository;
    private final KakaoRegionService kakaoRegionService;
    private final UserRepository userRepository;

    @Transactional
    public GetLocationWeatherShortDetailResponse getShortWeatherInfo() {

        //getLocationForWeatherQuery를 통해 쿼리에 필요한 Location 엔티티 가져옴
        Location locationToQuery = getLocationForWeatherQuery();

        return getWeatherInfoByLocation(locationToQuery.getId());

    }

    @Transactional
    public GetLocationWeatherShortDetailResponse getWeatherPreview(String b_code, double longitude, double latitude) {
        String bCode = b_code;

        //b_code가 비어있는지 확인(null, "", " " 모두 체크)
        if (!StringUtils.hasText(bCode)) {
            // b_code가 없다면, KakaoRegionService를 호출하여 좌표로 b_code를 얻어옴
            bCode = kakaoRegionService.getBCodeByCoordinates(longitude, latitude);
        }

        //b_code를 통해 Location 엔티티를 조회합니다.
        Location location = locationRepository.findLocationBybCode(bCode)
                .orElseThrow(() -> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));

        // 단기예보 조회와 이후 로직이 동일하기에 getWeatherInfoByLocation호출
        return getWeatherInfoByLocation(location.getId());
    }

    private GetLocationWeatherShortDetailResponse getWeatherInfoByLocation(Long locationId) {

        //쿼리에 필요한 파라미터를 준비
        LocalDate today = LocalDate.now();

        //준비된 파라미터로 단일 JPQL 쿼리를 호출하여 필요한 모든 데이터를 한 번에 가져옴
        List<Object[]> results = weatherRepository.findShortWeatherInfoByLocationId(locationId, today);

        if (results.isEmpty() || results.get(0)[0] == null) {
            throw new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND);
        }

        //결과에서 Location, DailySummary, 전체 단기예보 리스트를 분리
        Location location = (Location) results.get(0)[0];
        DailySummary summary = (DailySummary) results.get(0)[2];
        List<LocationWeatherShortDetail> allForecasts = results.stream()
                .map(row -> (LocationWeatherShortDetail) row[1])
                .filter(lwsd -> lwsd != null)
                .collect(Collectors.toList());

        if (summary == null) throw new BusinessException(WeatherErrorCode.SUMMARY_DATA_NOT_FOUND);
        if (allForecasts.isEmpty()) throw new BusinessException(WeatherErrorCode.WEATHER_DATA_NOT_FOUND);

        //현재 시간(HH00)을 기준으로, 그 시간 이후의 예보 데이터만 필터링
        int todayInt = Integer.parseInt(today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        int currentTime = Integer.parseInt(LocalTime.now().format(DateTimeFormatter.ofPattern("HH00")));
        List<LocationWeatherShortDetail> filteredForecasts = allForecasts.stream()
                .filter(f -> f.getDate() > todayInt || (f.getDate().equals(todayInt) && f.getTime() >= currentTime))
                .collect(Collectors.toList());

        // 분리된 엔티티와 필터링된 리스트를 Mapper에게 전달하여 최종 DTO를 생성
        return WeatherMapper.toShortWeatherResponse(location, summary, filteredForecasts);
    }


    @Transactional
    public List<GetWeatherMidDetailResponse> getMidWeatherInfo() {

        //getLocationForWeatherQuery를 통해 쿼리에 필요한 Location 엔티티 가져옴
        Location locationToQuery = getLocationForWeatherQuery();

        String midTermRegCode = locationToQuery.getMidTermRegCode();

        //늘 날짜와 7일 후 날짜를 'YYYYMMDD' 포맷의 정수로 준비
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        Integer startDate = Integer.parseInt(today.format(formatter));
        Integer endDate = Integer.parseInt(today.plusDays(7).format(formatter));

        //해당 지역의 1주일치 중기 예보 데이터를 조회
        List<WeatherMidDetail> forecastEntities = weatherMidDetailRepository
                .findByMidTermRegCodeAndDateBetweenOrderByDateAsc(midTermRegCode, startDate, endDate);

        //조회된 엔티티 리스트를 Stream을 사용하여 응답 DTO 리스트로 변환
        return forecastEntities.stream()
                .map(WeatherMapper::toMidWeatherResponse)
                .collect(Collectors.toList());
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
