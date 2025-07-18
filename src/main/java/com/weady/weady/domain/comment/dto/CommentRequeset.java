package com.weady.weady.domain.comment.dto;

import lombok.Builder;

public class CommentRequeset {

    @Builder
    public record CommentCreateRequestDTO(
            Long parentId,
            String content
    ){}
}
