package com.weady.weady.domain.curation.mapper;

import com.weady.weady.domain.curation.dto.CurationResponse;
import com.weady.weady.domain.curation.entity.CurationImg;

import java.util.List;
import java.util.stream.Collectors;

public class CurationMapper {

    //큐레이션 이미지 dto 형태로 변경
    public static List<CurationResponse.ImgDto> toImgDto(List<CurationImg> imgs){

        return  imgs.stream()
                .map(img -> CurationResponse.ImgDto.builder()
                        .imgUrl(img.getImgUrl())
                        .imgOrder(img.getImgOrder())
                        .build())
                .collect(Collectors.toList());

    }

    public static CurationResponse.curationByCurationIdResponseDto toCurationResponseDto(Long id,
                                                                                         String title,
                                                                                         List<CurationImg> imgs){


        return CurationResponse.curationByCurationIdResponseDto.builder()
                .curationId(id)
                .curationTitle(title)
                .imgs(toImgDto(imgs))
                .build();

    }
}
