package com.weady.weady.domain.fashion.controller;

import com.weady.weady.domain.fashion.dto.Response.FashionDetailResponseDto;
import com.weady.weady.domain.fashion.dto.Response.FashionSummaryResponseDto;
import com.weady.weady.domain.fashion.service.FashionService;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Fashion", description = "옷차림 관련 API")
@RequestMapping("/api/v1/fashion")
public class FashionController {
    private final FashionService fashionService;

    @Operation(summary = "옷차림 요약 정보 조회 API", description = "홈 화면에서 옷차림 요약 정보를 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FashionSummaryResponseDto>> getFashionSummary() {
        FashionSummaryResponseDto response = fashionService.getFashionSummary();

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(response));
    }

    @Operation(summary = "옷차림 상세 정보 조회 API", description = "옷차림 화면에서 옷차림 상세 정보를 조회합니다.")
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<FashionDetailResponseDto>> getFashionDetail() {
        FashionDetailResponseDto response = fashionService.getFashionDetail();

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(response));
    }
}
