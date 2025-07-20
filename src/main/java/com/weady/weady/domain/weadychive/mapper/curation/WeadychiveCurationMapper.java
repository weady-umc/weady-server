package com.weady.weady.domain.weadychive.mapper.curation;

import com.weady.weady.domain.curation.dto.CurationCategoryResponse;
import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.weadychive.dto.curation.WeadychiveCurationResponse;
import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;

import java.util.List;

public class WeadychiveCurationMapper {

    public static WeadychiveCurationResponse.scrappedCurationByUserResponseDto toScrappedCurationResponseDto(String userName,
                                                                                                             List<Curation> curations)
    {

        List<WeadychiveCurationResponse.CurationDto> curationDtos = curations.stream()
                .map(curation -> WeadychiveCurationResponse.CurationDto.builder()
                        .curationId(curation.getId())
                        .curationTitle(curation.getTitle())
                        .backgroundImgUrl(curation.getBackgroundImgUrl())
                        .build())
                .toList();


        return WeadychiveCurationResponse.scrappedCurationByUserResponseDto.builder()
                .userName(userName)
                .curations(curationDtos)
                .build();

    }

}
