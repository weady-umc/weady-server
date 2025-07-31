package com.weady.weady.domain.weadychive.controller.curation;



import com.weady.weady.domain.weadychive.dto.curation.Request.ScrapCurationRequestDto;
import com.weady.weady.domain.weadychive.dto.curation.Response.CurationDto;
import com.weady.weady.domain.weadychive.dto.curation.Response.ScrappedCurationByUserResponseDto;

import com.weady.weady.domain.weadychive.service.curation.WeadychiveCurationService;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/weadychive/curation")
@Tag(name = "Curation", description = "웨디카이브 큐레이션 관련 API")
public class WeadychiveController {

    private final WeadychiveCurationService weadychiveCurationService;

    @GetMapping("/my")
    @Operation(summary = "사용자 스크랩한 큐레이션 조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<ScrappedCurationByUserResponseDto>> getExpensesByTrip() {
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(weadychiveCurationService.getScrappedCuration()));
    }

    @PostMapping("/bookmarks")
    @Operation(summary = "큐레이션 스크랩하기 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<CurationDto>> bookmarkCuration(@Valid @RequestBody ScrapCurationRequestDto requestDto) {

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(weadychiveCurationService.scrapCuration(requestDto)));
    }

    @DeleteMapping("/bookmarks/{curationId}")
    @Operation(summary = "큐레이션 스크랩 취소하기 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<Void>> cancelBookmarkCuration(@PathVariable("curationId") Long curationId) {
        weadychiveCurationService.cancelCuration(curationId);

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(null));
    }

}
