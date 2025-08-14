package com.weady.weady.domain.board.controller;

import com.weady.weady.domain.board.dto.request.BoardCreateRequestDto;
import com.weady.weady.domain.board.dto.request.ReportRequestDto;
import com.weady.weady.domain.board.dto.response.BoardGoodResponseDto;
import com.weady.weady.domain.board.dto.response.BoardHomeResponseDto;
import com.weady.weady.domain.board.dto.response.BoardResponseDto;
import com.weady.weady.domain.board.service.BoardService;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Tag(name = "Board", description = "웨디보드 관련 API")
@RequestMapping("/api/v1/board")
public class BoardController {

    private final BoardService boardService;


    @GetMapping(value = "")
    @Operation(summary = "1. 보드 홈 게시물 전체 조회 API", description = "전체 게시물을 조회하는 API입니다. 날씨, 계절 태그 ID로 게시글을 필터링 할 수 있습니다.")
    public ResponseEntity<ApiResponse<Slice<BoardHomeResponseDto>>> getFilteredAndSortedBoards(
            @RequestParam(name = "seasonTagId", required = false) Long seasonTagId,
            @RequestParam(name = "weatherTagId", required = false) Long weatherTagId,
            @RequestParam(name = "temperatureTagId", required = false) Long temperatureTagId,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size
    ) {
        Slice<BoardHomeResponseDto> responseDtoList = boardService.getFilteredAndSortedBoards(seasonTagId, temperatureTagId, weatherTagId, size);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDtoList, "게시글 조회 성공!"));

    }


    @GetMapping(value = "/{boardId}")
    @Operation(summary = "2. 게시물 조회 API", description = "특정 게시물을 조회하는 API입니다.")
    @Parameters({
            @Parameter(name = "boardId", description = "게시물의 아이디, path variable 입니다.")})
    public ResponseEntity<ApiResponse<BoardResponseDto>> getPostById(@PathVariable(name = "boardId") Long boardId) {
        BoardResponseDto responseDto = boardService.getPostById(boardId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDto, "게시글 조회 성공!"));
    }


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "3. 게시물 작성 API", description = "로그인 한 사용자가 게시물을 작성하는 API입니다.")
    public ResponseEntity<ApiResponse<BoardResponseDto>> createPost(
            @RequestPart(value = "images") List<MultipartFile> images,

            @Parameter(
                    description = "이미지 파일 제외 게시물 작성 DTO",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart (value = "postData") BoardCreateRequestDto postData) {

        BoardResponseDto responseDto = boardService.createPost(images, postData);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDto, "게시글 작성 성공!"));
    }


    @PatchMapping(value = "/{boardId}")
    @Operation(summary = "4. 게시물 수정 API")
    public ResponseEntity<ApiResponse<BoardResponseDto>> updatePost(
            @PathVariable(name = "boardId") Long boardId,
            @RequestBody BoardCreateRequestDto requestDto) {
        BoardResponseDto responseDto = boardService.updatePost(requestDto, boardId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDto, "게시글 수정 성공!"));
    }


    @DeleteMapping(value = "/{boardId}")
    @Operation(summary = "5. 게시물 삭제 API")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable(name = "boardId") Long boardId) {
        boardService.deletePost(boardId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of("게시물 삭제 성공!"));
    }


    @PostMapping(value = "/{boardId}/good")
    @Operation(summary = "8. 게시물 좋아요 API")
    public ResponseEntity<ApiResponse<BoardGoodResponseDto>> addGood(@PathVariable(name = "boardId") Long boardId) {

        BoardGoodResponseDto responseDto = boardService.addGood(boardId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDto, "게시글 좋아요 성공!"));
    }

    @DeleteMapping(value = "/{boardId}/good")
    @Operation(summary = "9. 게시물 좋아요 취소 API")
    public ResponseEntity<ApiResponse<BoardGoodResponseDto>> cancelGood(@PathVariable(name = "boardId") Long boardId) {

        BoardGoodResponseDto responseDto = boardService.cancelGood(boardId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDto, "게시글 좋아요 취소 성공!"));
    }


    @PostMapping(value = "/{boardId}/report")
    @Operation(summary = "10. 게시물 신고 API")
    public ResponseEntity<ApiResponse<Void>> reportPost(
            @PathVariable(name = "boardId") Long boardId,
            @RequestBody ReportRequestDto requestDto) {
        boardService.reportPost(boardId, requestDto);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of("게시물 신고 성공!"));
    }


    @PostMapping(value = "/{boardId}/hide")
    @Operation(summary = "11. 게시물 숨김 API")
    public ResponseEntity<ApiResponse<Void>> hidePost(@PathVariable(name = "boardId") Long boardId) {
        boardService.hidePost(boardId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of("게시물 숨김 성공!"));
    }

    @DeleteMapping(value = "/{boardId}/hide")
    @Operation(summary = "12. 게시물 숨김 취소 API")
    public ResponseEntity<ApiResponse<Void>> unhidePost(@PathVariable(name = "boardId") Long boardId) {
        boardService.unhidePost(boardId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of("게시물 숨김 취소 성공!"));
    }
}

