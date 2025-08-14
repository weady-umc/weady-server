package com.weady.weady.domain.curation.dto.Response;

import lombok.Builder;

@Builder
public record ImgDto(String imgUrl,
                     int imgOrder,
                     String imgAddress
){}