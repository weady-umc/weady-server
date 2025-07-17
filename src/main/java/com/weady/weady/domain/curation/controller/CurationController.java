package com.weady.weady.domain.curation.controller;


import com.weady.weady.domain.curation.dto.CurationCategoryResponse;
import com.weady.weady.domain.curation.dto.CurationResponse;
import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.curation.service.CurationService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
import com.weady.weady.global.common.apiResponse.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/curation")
@Tag(name = "Curation", description = "큐레이션 관련 API")
public class CurationController {

    private final CurationService curationService;


    @GetMapping(" ")
    @Operation(summary = "큐레이션 카테고리 조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<List<CurationCategoryResponse.curationCategoryResponseDto>> getExpensesByTrip(){
        return ApiSuccessResponse.of(curationService.getCurationCategory());
    }

    @GetMapping("/{curationId}")
    @Operation(summary = "큐레이션 상세조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<CurationResponse.curationByCurationIdResponseDto> getExpensesByTrip(@PathVariable("curationId") Long curationId){
        return ApiSuccessResponse.of(curationService.getSpecificCuration(curationId));
    }
}
