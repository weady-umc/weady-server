package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentResponseDto(
        Long commentId,
        Long parentId,
        String username,
        String profileImageUrl,
        String content,
        LocalDateTime createdAt
) {}
