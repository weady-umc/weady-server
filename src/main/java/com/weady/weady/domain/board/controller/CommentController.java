package com.weady.weady.domain.board.controller;


import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import com.weady.weady.domain.board.dto.request.CommentCreateRequestDto;
import com.weady.weady.domain.board.dto.response.CommentResponseDto;
import com.weady.weady.domain.board.dto.response.CommentWithChildResponseDto;
import com.weady.weady.domain.board.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "웨디보드 댓글 관련 API")
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    @GetMapping(value = "/board/{boardId}/comments")
    @Operation(summary = "1. 댓글 전체 조회 API")
    public ResponseEntity<ApiResponse<Slice<CommentWithChildResponseDto>>> getComments(
            @PathVariable(name = "boardId") Long boardId,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size
    ) {
        Slice<CommentWithChildResponseDto> responseDtoList = commentService.getComments(boardId, size);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(responseDtoList, "댓글 조회 성공!"));
    }


    @PostMapping(value = "/board/{boardId}/comments")
    @Operation(summary = "2. 댓글 작성 API", description = "댓글 및 대댓글을 작성하는 API입니다.")
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @PathVariable(name = "boardId") Long boardId,
            @RequestBody CommentCreateRequestDto requestDto) {
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(commentService.createComment(requestDto, boardId), "댓글 작성 성공!"));
    }

    @DeleteMapping(value = "/board/comments/{commentId}")
    @Operation(summary = "3. 댓글 삭제 API")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable(name = "commentId") Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of("댓글 삭제 성공!"));
    }

}
