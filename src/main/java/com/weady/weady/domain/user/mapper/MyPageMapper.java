package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.board.dto.response.BoardImgResponseDto;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardImg;
import com.weady.weady.domain.user.dto.response.GetBoardInMyPageResponse;
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

    public static GetMyPageResponse.CalendarResponse toCalendarResponse(LocalDate date, String thumbnailUrl, Long weatherTagId, Boolean isPublic) {
        return GetMyPageResponse.CalendarResponse.builder()
                .date(date)
                .thumbnailUrl(thumbnailUrl)
                .weatherTagId(weatherTagId)
                .isPublic(isPublic)
                .build();
    }

    public static GetBoardInMyPageResponse toGetBoardInMyPageResponse(Board board, List<BoardImg> images) {
        List<BoardImgResponseDto> imageList = images.stream()
                .map(img -> BoardImgResponseDto.builder()
                        .imgUrl(img.getImgUrl())
                        .imgOrder(img.getImgOrder())
                        .build())
                .toList();

        Long weatherTagId = (board.getWeatherTag() != null) ? board.getWeatherTag().getId() : null;

        return GetBoardInMyPageResponse.builder()
                .boardId(board.getId())
                .createdAt(board.getCreatedAt())
                .isPublic(board.getIsPublic())
                .weatherTagId(weatherTagId)
                .imageList(imageList)
                .build();
    }
}
