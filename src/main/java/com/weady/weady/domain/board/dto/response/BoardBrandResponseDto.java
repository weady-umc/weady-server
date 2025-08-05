package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

@Builder
public record BoardBrandResponseDto(
        String brand,
        String product
) {}
