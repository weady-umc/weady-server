package com.weady.weady.domain.curation.mapper;

import com.weady.weady.domain.curation.dto.CurationCategoryResponse;
import com.weady.weady.domain.curation.entity.CurationCategory;

import java.util.List;

public class CurationCategoryMapper {

    public static CurationCategoryResponse.curationCategoryResponseDto toCurationCategoryResponseDto(Long locationId,
                                                                                                     String locationName)
    {
        return CurationCategoryResponse.curationCategoryResponseDto.builder()
                .locationId(locationId)
                .locationName(locationName)
                .build();
    }

    public static List<CurationCategoryResponse.curationCategoryResponseDto> toDtoList(List<CurationCategory> entities) {
        return entities.stream()
                .map(entity -> toCurationCategoryResponseDto(entity.getId(), entity.getViewName()))
                .toList();
    }
}
