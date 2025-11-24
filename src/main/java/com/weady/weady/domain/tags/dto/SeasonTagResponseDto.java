package com.weady.weady.domain.tags.dto;

import lombok.Builder;

@Builder
public record SeasonTagResponseDto(Long id,
                                   String name) {
}
