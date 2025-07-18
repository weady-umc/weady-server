package com.weady.weady.domain.comment.dto;

import lombok.Builder;

import java.time.LocalDateTime;

public class CommentResponse {

    @Builder
    public record CommentResponseDTO(
            Long commentId,
            Long parentId,
            String content,
            String username,
            String profileImgUrl,
            LocalDateTime createdAt

    ){}
}
