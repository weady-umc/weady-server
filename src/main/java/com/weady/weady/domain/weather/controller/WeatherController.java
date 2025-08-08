package com.weady.weady.domain.weather.controller;

import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import com.weady.weady.domain.weather.dto.response.GetLocationWeatherShortDetailResponse;
import com.weady.weady.domain.weather.dto.response.GetWeatherMidDetailResponse;
import com.weady.weady.domain.weather.dto.response.LocationTagResponseDto;
import com.weady.weady.domain.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/short")
    @Operation(summary = "단기예보 날씨 정보 조회 API")
    public ResponseEntity<ApiResponse<GetLocationWeatherShortDetailResponse>> getLocationWeatherShortDetail() {

        GetLocationWeatherShortDetailResponse responseData = weatherService.getShortWeatherInfo();
        ApiResponse<GetLocationWeatherShortDetailResponse> responseWrapper = ApiSuccessResponse.of(responseData, "단기 예보 조회에 성공했습니다.");

        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }

    @GetMapping("/preview")
    @Operation(summary = "지역 날씨 미리보기 API")
    public ResponseEntity<ApiResponse<GetLocationWeatherShortDetailResponse>> getWeatherPreview(
            @RequestParam(required = false) String b_code,
            @RequestParam Double x,
            @RequestParam Double y
    ){

        GetLocationWeatherShortDetailResponse responseData = weatherService.getWeatherPreview(b_code, x, y);
        ApiResponse<GetLocationWeatherShortDetailResponse> responseWrapper = ApiSuccessResponse.of(responseData, "지역 날씨 미리보기에 성공했습니다.");

        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }

    @GetMapping("/mid-term")
    @Operation(summary = "중기예보 날씨 정보 조회 API")
    public ResponseEntity<ApiResponse<List<GetWeatherMidDetailResponse>>> getWeatherMidDetail() {

        List<GetWeatherMidDetailResponse> responseData = weatherService.getMidWeatherInfo();
        ApiResponse<List<GetWeatherMidDetailResponse>> responseWrapper = ApiSuccessResponse.of(responseData,"중기 예보 조회에 성공했습니다.");

        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }

    @GetMapping("/daily-summary")
    @Operation(summary = "현재 위치의 계절, 기온, 날씨 태그 조회 API")
    public ResponseEntity<ApiResponse<LocationTagResponseDto>> getLocationTag() {

        LocationTagResponseDto responseDto = weatherService.getLocationTag();
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDto));
    }
}
