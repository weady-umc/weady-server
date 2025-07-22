package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

@Builder
public record BoardImgResponseDto(
        String imgUrl,
        Integer imgOrder
){}
