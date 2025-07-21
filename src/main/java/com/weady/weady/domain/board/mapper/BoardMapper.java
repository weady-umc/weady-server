package com.weady.weady.domain.board.mapper;

import com.weady.weady.domain.board.dto.BoardRequest;
import com.weady.weady.domain.board.dto.BoardResponse;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardPlace;
import com.weady.weady.domain.board.entity.board.BoardStyle;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    public static Board toBoard(BoardRequest.BoardCreateRequestDto request, User user,
                                SeasonTag seasonTag, TemperatureTag temperatureTag, WeatherTag weatherTag) {
        return Board.builder()
                .isPublic(request.isPublic())
                .content(request.content())
                .seasonTag(seasonTag)
                .temperatureTag(temperatureTag)
                .weatherTag(weatherTag)
                .user(user)
                .build();
    }

    public static List<BoardPlace> toBoardPlaceList(List<BoardRequest.BoardPlaceRequestDto> requestDtos) {
        return requestDtos.stream()
                .map(dto -> BoardPlace.builder()
                        .placeName(dto.placeName())
                        .placeAddress(dto.placeAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<BoardStyle> toBoardStyleList(List<ClothesStyleCategory> styles) {
        return styles.stream()
                .map(style -> BoardStyle.builder()
                        .clothesStyleCategory(style)
                        .build())
                .collect(Collectors.toList());
    }


    // 응답 dto
    public static BoardResponse.BoardResponseDto toBoardResponseDto(Board board, User user) {
        return BoardResponse.BoardResponseDto.builder()
                .boardId(board.getId())
                .isPublic(board.getIsPublic())
                .content(board.getContent())
                .userName(user.getName())
                .userProfileImageUrl(user.getProfileImgUrl())
                .likeCount(board.getGoodCount())
                .placeDtoList(toBoardPlaceResponseListDto(board.getBoardPlaceList()))
                .styleIdList(toBoardStyleResponseListDto(board.getBoardStyleList()))
                .createdAt(board.getCreatedAt())
                .build();
    }


    public static List<BoardResponse.BoardPlaceResponseDto> toBoardPlaceResponseListDto(List<BoardPlace> places){
        return places.stream()
                .map(place -> BoardResponse.BoardPlaceResponseDto.builder()
                        .placeName(place.getPlaceName())
                        .placeAddress(place.getPlaceAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<BoardResponse.BoardStyleResponseDto> toBoardStyleResponseListDto(List<BoardStyle> styles){
        return styles.stream()
                .map(style -> BoardResponse.BoardStyleResponseDto.builder()
                        .styleTagId(style.getClothesStyleCategory().getId())
                        .build())
                .collect(Collectors.toList());
    }
}
