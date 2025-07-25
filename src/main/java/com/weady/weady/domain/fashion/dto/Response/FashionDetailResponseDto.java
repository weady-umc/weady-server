package com.weady.weady.domain.fashion.dto.Response;

import java.util.List;

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

    public record Recommendation(int time,
                                 float feelTmp,
                                 Clothing clothing
    ) {}

    public record ChartPoint(int time,
                             float feelTmp,
                             Clothing clothing
    ) {}

    public record Clothing(String name,
                           String imageUrl
    ) {}

    public record Tags(Tag season,
                       Tag weather,
                       Tag temperature
    ) {}

    public record Tag(Long id,
                      String name
    ) {}
}
