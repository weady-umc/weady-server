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
import com.weady.weady.domain.user.dto.ExampleUserResponse;

import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    public static Board toBoard(BoardRequest.BoardCreateRequestDto request,
                                SeasonTag seasonTag, TemperatureTag temperatureTag, WeatherTag weatherTag) {
        return Board.builder()
                .isPublic(request.isPublic())
                .content(request.content())
                .seasonTag(seasonTag)
                .temperatureTag(temperatureTag)
                .weatherTag(weatherTag)
                .build();
    }

    public static List<BoardPlace> toBoardPlaceList(List<BoardRequest.BoardPlaceRequestDto> requestDtos, Board board) {
        return requestDtos.stream()
                .map(dto -> BoardPlace.builder()
                        .placeName(dto.placeName())
                        .placeAddress(dto.placeAddress())
                        .board(board) //연관관계 주입
                        .build())
                .collect(Collectors.toList());
    }

    public static List<BoardStyle> toBoardStyleList(List<ClothesStyleCategory> styles, Board board) {
        return styles.stream()
                .map(style -> BoardStyle.builder()
                        .board(board)
                        .clothesStyleCategory(style)
                        .build())
                .collect(Collectors.toList());
    }


    // 응답 dto
    public static BoardResponse.BoardResponseDto toBoardResponseDto(Board board, List<BoardResponse.BoardPlaceResponseDto> places,
                                                                    List<BoardResponse.BoardStyleResponseDto> styles,
                                                                    ExampleUserResponse.ExampleUserResponseDto userResponseDto, int likeCount) {
        return BoardResponse.BoardResponseDto.builder()
                .boardId(board.getId())
                .isPublic(board.isPublic())
                .content(board.getContent())
                .userResponseDto(userResponseDto)
                .likeCount(likeCount)
                .placeDtoList(places)
                .styleIdList(styles)
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
