package com.weady.weady.domain.tags.dto;

import lombok.Builder;

@Builder
public record TemperatureTagResponseDto(Long id,
                                        String name,
                                        Integer minTemperature,
                                        Integer maxTemperature) {
}
