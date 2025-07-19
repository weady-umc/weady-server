package com.weady.weady.domain.board.controller;

import com.weady.weady.domain.board.dto.BoardRequest;
import com.weady.weady.domain.board.dto.BoardResponse;
import com.weady.weady.domain.board.service.BoardService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
import com.weady.weady.global.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.global.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
@Tag(name = "Board", description = "보드 관련 API")
@RequestMapping("/api/v1/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시물 작성 api", description = "로그인 한 사용자가 게시물을 작성하는 api입니다.")
    public ResponseEntity<ApiResponse<BoardResponse.BoardResponseDto>> createPost (
            //@RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestBody BoardRequest.BoardCreateRequestDto postData){
        BoardResponse.BoardResponseDto responseDto = boardService.createPost(postData);

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDto, "게시글 작성 성공!"));
    }
}
