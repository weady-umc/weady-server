package com.weady.weady.domain.weather.service;

import com.weady.weady.common.error.errorCode.LocationErrorCode;
import com.weady.weady.common.error.errorCode.WeatherErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.weather.dto.response.GetLocationWeatherShortDetailResponse;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.mapper.WeatherMapper;
import com.weady.weady.domain.weather.repository.WeatherMainRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherMainRepository weatherRepository;

    @Transactional
    public GetLocationWeatherShortDetailResponse getShortWeatherInfo(Long locationId) {
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

}
