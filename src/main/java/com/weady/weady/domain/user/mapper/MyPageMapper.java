package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.user.dto.response.GetMyPageResponse;
import com.weady.weady.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public class MyPageMapper {

    public static GetMyPageResponse toGetMyPageResponse(User user, List<GetMyPageResponse.CalendarResponse> calendar) {
        return GetMyPageResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .calendar(calendar)
                .build();
    }

    public static GetMyPageResponse.CalendarResponse toCalendarResponse(LocalDate date, String thumbnailUrl) {
        return GetMyPageResponse.CalendarResponse.builder()
                .date(date)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
