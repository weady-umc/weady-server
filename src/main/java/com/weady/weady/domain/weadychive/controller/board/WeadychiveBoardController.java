package com.weady.weady.domain.weadychive.controller.board;

import com.weady.weady.domain.weadychive.dto.board.request.ScrapBoardRequestDto;
import com.weady.weady.domain.weadychive.dto.board.response.ScrapBoardResponseDto;
import com.weady.weady.domain.weadychive.dto.board.response.ScrappedBoardByUserResponseDto;
import com.weady.weady.domain.weadychive.dto.curation.Response.ScrappedCurationByUserResponseDto;
import com.weady.weady.domain.weadychive.service.board.WeadychiveBoardService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
import com.weady.weady.global.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.global.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Tag(name = "Board", description = "웨디카이브 보드 관련 API")
@RequestMapping("/api/v1/weadychive/board")
public class WeadychiveBoardController {

    private final WeadychiveBoardService weadychiveBoardService;

    @GetMapping("/my")
    @Operation(summary = "사용자 스크랩한 보드 조회 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<Slice<ScrappedBoardByUserResponseDto>>> getScrappedBoard(
            @Valid @RequestParam(name = "size", required = false, defaultValue = "10") Integer size
    ) {
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(weadychiveBoardService.getScrappedBoard(size)));
    }



    @PostMapping("/bookmarks")
    @Operation(summary = "게시물 스크랩하기 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<ScrapBoardResponseDto>> scrapBoard(@Valid @RequestBody ScrapBoardRequestDto requestDto) {

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(weadychiveBoardService.scrapBoard(requestDto)));
    }

    @DeleteMapping("/bookmarks")
    @Operation(summary = "게시물 스크랩 취소하기 API")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ResponseEntity<ApiResponse<ScrapBoardResponseDto>> cancelScrapBoard(@Valid @RequestBody ScrapBoardRequestDto requestDto) {

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(weadychiveBoardService.cancelScrapBoard(requestDto)));
    }
}
