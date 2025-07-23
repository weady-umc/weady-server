package com.weady.weady.domain.curation.mapper;


import com.weady.weady.domain.curation.dto.Response.CurationCategoryResponseDto;
import com.weady.weady.domain.curation.entity.CurationCategory;

import java.util.List;

public class CurationCategoryMapper {

    public static CurationCategoryResponseDto toCurationCategoryResponseDto(Long locationId,
                                                                            String locationName)
    {
        return CurationCategoryResponseDto.builder()
                .locationId(locationId)
                .locationName(locationName)
                .build();
    }

    public static List<CurationCategoryResponseDto> toDtoList(List<CurationCategory> entities) {
        return entities.stream()
                .map(entity -> toCurationCategoryResponseDto(entity.getId(), entity.getViewName()))
                .toList();
    }
}
