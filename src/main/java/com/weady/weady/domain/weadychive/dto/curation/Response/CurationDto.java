package com.weady.weady.domain.weadychive.dto.curation.Response;

import lombok.Builder;

@Builder
public record CurationDto(
        Long curationId,
        String curationTitle,
        String backgroundImgUrl
) {}
