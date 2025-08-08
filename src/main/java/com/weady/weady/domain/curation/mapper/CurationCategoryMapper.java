package com.weady.weady.domain.curation.mapper;


import com.weady.weady.domain.curation.dto.Response.*;
import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.curation.entity.CurationImg;

import java.util.List;
import java.util.stream.Collectors;

public class CurationCategoryMapper {

    public static CurationCategoryResponseDto toCurationCategoryResponseDto(Long curationCategoryId,
                                                                            String locationName)
    {
        return CurationCategoryResponseDto.builder()
                .curationCategoryId(curationCategoryId)
                .locationName(locationName)
                .build();
    }

    public static List<CurationCategoryResponseDto> toDtoList(List<CurationCategory> entities) {
        return entities.stream()
                .map(entity -> toCurationCategoryResponseDto(entity.getId(), entity.getViewName()))
                .toList();
    }


    //큐레이션 이미지 dto 형태로 변경
    public static List<CurationDto> toCurationDto(List<Curation> curations){

        return  curations.stream()
                .map(curation -> CurationDto.builder()
                        .curationId(curation.getId())
                        .curationTitle(curation.getTitle())
                        .backgroundImgUrl(curation.getBackgroundImgUrl())
                        .build())
                .collect(Collectors.toList());

    }

    public static CurationByLocationResponseDto toCurationByLocationResponseDto(Long locationId,
                                                                        String locationName,
                                                                        String season,
                                                                        String weather,
                                                                        List<Curation> curations){


        return CurationByLocationResponseDto.builder()
                .locationId(locationId)
                .locationName(locationName)
                .season(season)
                .weather(weather)
                .curations(toCurationDto(curations))
                .build();

    }
}
