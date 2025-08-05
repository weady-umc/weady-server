package com.weady.weady.domain.board.dto.request;

import lombok.Builder;

@Builder
public record BoardBrandRequestDto(
        String brand,
        String product
) {}
