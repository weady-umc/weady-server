package com.weady.weady.domain.board.dto.response;

import com.weady.weady.domain.board.entity.comment.BoardComment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentWithChildResponseDto(
        Long commentId,
        Long parentId,
        String username,
        String profileImageUrl,
        String content,
        List<CommentResponseDto> childCommentsList,
        LocalDateTime createdAt
) {}
