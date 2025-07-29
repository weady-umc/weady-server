package com.weady.weady.domain.weather.dto.response;

import com.weady.weady.domain.weather.entity.SkyCode;
import lombok.Builder;

import java.util.List;

@Builder
public record GetLocationWeatherShortDetailResponse(

        //지역 이름
        String address1,

        String address2,

        String address3,

        //현재 온도
        Float currentTmp,

        //현재 하늘 상태
        SkyCode skyStatus,

        //오늘 최고온도
        Float maxTmp,

        //오늘 최저온도
        Float minTmp,

        //시간대별 날씨
        List<HourlyForecast> hourlyForecasts,

        //시간대별 강수확률
        List<HourlyPrecipitation> hourlyPrecipitations,

        //시간대별 풍속,풍향
        List<HourlyWind> hourlyWinds

) {
    @Builder
    public record HourlyForecast(

            //예보 시간
            Integer time,

            //하늘 상태
            SkyCode skyStatus,

            //기온
            Float tmp
    ) {
    }
    @Builder
    public record HourlyPrecipitation(

            //예보 시간
            Integer time,

            //강수확률
            Float probability
    ) {
    }
    @Builder
    public record HourlyWind(
            //예보 시간
            Integer time,

            //풍향
            String direction,

            //풍속
            Float speed
    ) {
    }
}
