package com.weady.weady.domain.fashion.dto.Response;

import lombok.Builder;

@Builder
public record FashionSummaryResponseDto(Long locationId,
                                        String recommendation,
                                        String imageUrl) {
}
