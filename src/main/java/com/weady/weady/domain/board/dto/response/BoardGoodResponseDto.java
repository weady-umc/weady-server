package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

@Builder
public record BoardGoodResponseDto(
        Boolean liked,
        Integer goodCount
){}