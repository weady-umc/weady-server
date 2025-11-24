package com.weady.weady.domain.weather.mapper;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.weather.dto.response.GetLocationWeatherShortDetailResponse;
import com.weady.weady.domain.weather.dto.response.GetWeatherMidDetailResponse;
import com.weady.weady.domain.weather.dto.response.LocationTagResponseDto;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.domain.weather.entity.WeatherMidDetail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WeatherMapper {
    public static GetLocationWeatherShortDetailResponse toShortWeatherResponse(
            Location location,
            DailySummary summary,
            List<LocationWeatherShortDetail> filteredForecasts
    ) {
        //현재 날씨 정보는 필터링된 리스트 첫 번째
        LocationWeatherShortDetail currentWeather = filteredForecasts.get(0);

        // 시간대별 상세 정보 DTO 리스트들을 생성
        List<GetLocationWeatherShortDetailResponse.HourlyForecast> hourlyForecasts = filteredForecasts.stream()
                .map(f -> GetLocationWeatherShortDetailResponse.HourlyForecast.builder()
                        .time(f.getTime())
                        .skyStatus(f.getSkyCode())
                        .tmp(f.getTmp())
                        .build())
                .collect(Collectors.toList());

        List<GetLocationWeatherShortDetailResponse.HourlyPrecipitation> hourlyPrecipitations = filteredForecasts.stream()
                .map(f -> GetLocationWeatherShortDetailResponse.HourlyPrecipitation.builder()
                        .time(f.getTime())
                        .probability(f.getPop())
                        .build())
                .collect(Collectors.toList());

        List<GetLocationWeatherShortDetailResponse.HourlyWind> hourlyWinds = filteredForecasts.stream()
                .map(f -> GetLocationWeatherShortDetailResponse.HourlyWind.builder()
                        .time(f.getTime())
                        .direction(convertWindDirection(f.getVec()))
                        .speed(f.getWsd())
                        .build())
                .collect(Collectors.toList());

        return GetLocationWeatherShortDetailResponse.builder()
                .address1(location.getAddress1())
                .address2(location.getAddress2())
                .address3(location.getAddress3())
                .currentTmp(currentWeather.getTmp())
                .skyStatus(currentWeather.getSkyCode())
                .maxTmp(summary.getActualTmx())
                .minTmp(summary.getActualTmn())
                .hourlyForecasts(hourlyForecasts)
                .hourlyPrecipitations(hourlyPrecipitations)
                .hourlyWinds(hourlyWinds)
                .build();
    }

    public static GetWeatherMidDetailResponse toMidWeatherResponse(WeatherMidDetail weatherMid) {

        LocalDate localDate = LocalDate.parse(String.valueOf(weatherMid.getDate()));

        return GetWeatherMidDetailResponse.builder()
                .dayOfWeek(localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN))
                .amSkyStatus(weatherMid.getAmSkyCode())
                .pmSkyStatus(weatherMid.getPmSkyCode())
                .minTemp(weatherMid.getTmn())
                .maxTemp(weatherMid.getTmx())
                .build();
    }

    public static LocationTagResponseDto toLocationTagResponse(DailySummary dailySummary) {

        return LocationTagResponseDto.builder()
                .seasonTagId(dailySummary.getSeasonTag().getId())
                .temperatureTagId(dailySummary.getTemperatureTag().getId())
                .weatherTagId(dailySummary.getWeatherTag().getId())
                .build();
    }

    //풍향 각도를 8방위 문자열로 변환하는 헬퍼 메소드
    private static String convertWindDirection(Float degree) {
        if (degree == null) return "정보 없음";
        int val = (int) ((degree + 22.5) / 45);
        String[] directions = {"북풍", "북동풍", "동풍", "남동풍", "남풍", "남서풍", "서풍", "북서풍"};
        return directions[val % 8];
    }
}
