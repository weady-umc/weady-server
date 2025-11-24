package com.weady.weady.common.external.kakao;

import java.util.List;

public record KakaoApiResponse(List<RegionDocument> documents) {

    public record RegionDocument(
            String code,
            String region_type,
            String address_name
    ) {}
}