package com.weady.weady.domain.tags.dto;

import lombok.Builder;

@Builder
public record WeatherTagResponseDto(Long id,
                                    String name) {
}
