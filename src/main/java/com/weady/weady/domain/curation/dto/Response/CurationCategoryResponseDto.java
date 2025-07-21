package com.weady.weady.domain.curation.dto.Response;

import lombok.Builder;

@Builder
public record CurationCategoryResponseDto(Long locationId,
                                          String locationName
) {}