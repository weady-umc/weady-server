package com.weady.weady.domain.curation.dto.Response;

import lombok.Builder;

@Builder
public record CurationDto(
        Long curationId,
        String curationTitle,
        String bannerImgUrl,
        String backgroundImgUrl
) {}
