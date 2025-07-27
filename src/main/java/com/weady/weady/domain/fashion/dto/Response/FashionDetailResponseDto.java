package com.weady.weady.domain.fashion.dto.Response;

import lombok.Builder;

import java.util.List;

@Builder
public record FashionDetailResponseDto(Long locationId,
                                       String locationBCode,
                                       String address1,
                                       String address2,
                                       String address3,
                                       String address4,
                                       Recommendation recommendation,
                                       List<ChartPoint> chart,
                                       Tags tags
                                       ) {

    @Builder
    public record Recommendation(int time,
                                 float feelTmp,
                                 Clothing clothing
    ) {}

    @Builder
    public record ChartPoint(int time,
                             float feelTmp,
                             Clothing clothing
    ) {}

    @Builder
    public record Clothing(String name,
                           String imageUrl
    ) {}

    @Builder
    public record Tags(Tag season,
                       Tag weather,
                       Tag temperature
    ) {}

    @Builder
    public record Tag(Long id,
                      String name
    ) {}
}
