package com.weady.weady.domain.weather.dto.response;

import com.weady.weady.domain.weather.entity.SkyCode;
import lombok.Builder;

@Builder
public record GetWeatherMidDetailResponse(
        //요일
        String dayOfWeek,

        //오전 하늘상태
        SkyCode amSkyStatus,

        //오후 하늘 상태
        SkyCode pmSkyStatus,

        //최저기온
        Float minTemp,

        //최고기온
        Float maxTemp
) {
}
