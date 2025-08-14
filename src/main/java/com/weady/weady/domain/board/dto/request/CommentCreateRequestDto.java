package com.weady.weady.domain.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CommentCreateRequestDto(
        Long parentId,
        @NotBlank(message = "최소 한 글자 이상을 입력해야 합니다.")
        String content
) {}
