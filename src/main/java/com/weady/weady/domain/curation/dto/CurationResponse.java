package com.weady.weady.domain.curation.dto;

import lombok.Builder;

import java.util.List;

public class CurationResponse {

    @Builder
    public record curationByCurationIdResponseDto(Long curationId,
                                                  String curationTitle,
                                                  List<ImgDto> imgs

    ){}

    @Builder
    public record ImgDto(String imgUrl,
                         int imgOrder
    ){}
}
