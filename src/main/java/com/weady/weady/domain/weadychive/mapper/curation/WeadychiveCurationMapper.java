package com.weady.weady.domain.weadychive.mapper.curation;


import com.weady.weady.domain.curation.entity.Curation;

import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.weadychive.dto.curation.Response.CurationDto;
import com.weady.weady.domain.weadychive.dto.curation.Response.ScrappedCurationByUserResponseDto;


import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;

import java.util.List;

public class WeadychiveCurationMapper {

    public static ScrappedCurationByUserResponseDto toScrappedCurationResponseDto(String userName,
                                                                                  List<Curation> curations)
    {

        List<CurationDto> curationDtos = curations.stream()
                .map(curation -> CurationDto.builder()
                        .curationId(curation.getId())
                        .curationTitle(curation.getTitle())
                        .backgroundImgUrl(curation.getBackgroundImgUrl())
                        .build())
                .toList();


        return ScrappedCurationByUserResponseDto.builder()
                .userName(userName)
                .curations(curationDtos)
                .build();

    }

    public static WeadychiveCuration toEntity(User user, Curation curation){
        return WeadychiveCuration.builder()
                .user(user)
                .curation(curation)
                .build();
    }

    public static CurationDto toCurationResponseDto(Long curationId,
                                                                               String curationTitle,
                                                                               String ImgUrl) {

        return CurationDto.builder()
                .curationId(curationId)
                .curationTitle(curationTitle)
                .backgroundImgUrl(ImgUrl)
                .build();
    }


}
