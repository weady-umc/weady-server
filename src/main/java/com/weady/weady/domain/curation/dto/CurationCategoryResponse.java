package com.weady.weady.domain.curation.dto;

import lombok.Builder;

import java.util.List;

public class CurationCategoryResponse {

    @Builder
    public record curationByLocationResponseDto(Long locationId,
                                                String locationName,
                                                List<CurationDto> curations


    ){}

    @Builder
    public record CurationDto(
            Long curationId,
            String curationTitle,
            String backgroundImgUrl
    ) {}
}
