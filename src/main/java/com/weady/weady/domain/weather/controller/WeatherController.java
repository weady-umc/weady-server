package com.weady.weady.domain.weather.controller;

import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import com.weady.weady.domain.weather.dto.response.GetLocationWeatherShortDetailResponse;
import com.weady.weady.domain.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/{locationId}")
    @Operation(summary = "단기예보 날씨 정보 조회 API")
    public ResponseEntity<ApiResponse<GetLocationWeatherShortDetailResponse>> getLocationWeatherShortDetail(@PathVariable Long locationId) {

        GetLocationWeatherShortDetailResponse responseData = weatherService.getShortWeatherInfo(locationId);
        ApiResponse<GetLocationWeatherShortDetailResponse> responseWrapper = ApiSuccessResponse.of(responseData, "메인 날씨 정보 조회에 성공했습니다.");

        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }
}
