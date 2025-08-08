package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

@Builder
public record BoardImgResponseDto(

        Integer imgOrder,
        String imgUrl
){}
