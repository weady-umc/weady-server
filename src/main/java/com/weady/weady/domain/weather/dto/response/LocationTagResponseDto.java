package com.weady.weady.domain.weather.dto.response;

import lombok.Builder;

@Builder
public record LocationTagResponseDto(Long seasonTagId,
                                     Long temperatureTagId,
                                     Long weatherTagId) {
}
