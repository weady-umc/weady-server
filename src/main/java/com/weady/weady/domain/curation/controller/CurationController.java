package com.weady.weady.domain.curation.controller;



import com.weady.weady.domain.curation.dto.Request.CurationRequestDto;
import com.weady.weady.domain.curation.dto.Response.CurationByCurationIdResponseDto;
import com.weady.weady.domain.curation.dto.Response.CurationByLocationResponseDto;
import com.weady.weady.domain.curation.dto.Response.CurationCategoryResponseDto;
import com.weady.weady.domain.curation.service.CurationService;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/curation")
@Tag(name = "Curation", description = "큐레이션 관련 API")
public class CurationController {

    private final CurationService curationService;


    @GetMapping("/curationCategory")
    @Operation(summary = "큐레이션 카테고리 조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<List<CurationCategoryResponseDto>>> getCurationCategory(){
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(curationService.getCurationCategory()));
    }

    @GetMapping("/{curationId}")
    @Operation(summary = "큐레이션 상세조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<CurationByCurationIdResponseDto>> getSpecificCurationByCurationId(@PathVariable("curationId") Long curationId){
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(curationService.getSpecificCuration(curationId)));
    }

    @GetMapping("/curationCategory/{curationCategoryId}")
    @Operation(summary = "큐레이션 카테고리를 통한 큐레이션 조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<CurationByLocationResponseDto>> getCurationByCurationCategoryId(@PathVariable("curationCategoryId") Long curationCategoryId){
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(curationService.getCurationByCurationCategoryId(curationCategoryId)));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "지역별 큐레이션 조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<CurationByLocationResponseDto>> getCurationByLocationId(@PathVariable("locationId") Long locationId){
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(curationService.getCurationByLocation(locationId)));
    }


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "큐레이션 업로드 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<Void>> uploadCuration(
            @RequestPart("backgroundImage") MultipartFile backgroundImage,
            @RequestPart("contentImages") List<MultipartFile> contentImages,
            @RequestPart("postData") CurationRequestDto postData
    ){
        curationService.saveCuration(backgroundImage, contentImages, postData);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of("큐레이션 등록 성공"));
    }


}
