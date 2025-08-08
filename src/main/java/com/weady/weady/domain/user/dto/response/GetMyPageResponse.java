package com.weady.weady.domain.user.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GetMyPageResponse(Long userId,
                                String name,
                                String profileImageUrl,
                                List<CalendarResponse> calendar) {

    @Builder
    public record CalendarResponse(LocalDate date,
                                   String thumbnailUrl
    ) {}
}
