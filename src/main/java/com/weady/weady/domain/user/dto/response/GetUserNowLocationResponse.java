package com.weady.weady.domain.user.dto.response;

import lombok.Builder;

@Builder
public record GetUserNowLocationResponse(
        String bCode,
        String locationAddress1,
        String locationAddress2,
        String locationAddress3,
        String locationAddress4,
        Float currentTemp,
        Float actualTmx,
        Float actualTmn
) {
}
