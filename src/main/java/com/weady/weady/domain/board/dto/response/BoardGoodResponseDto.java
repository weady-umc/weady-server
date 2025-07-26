package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

@Builder
public record BoardGoodResponseDto(
        Boolean goodStatus,
        Integer goodCount
){}