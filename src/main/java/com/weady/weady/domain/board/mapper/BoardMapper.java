package com.weady.weady.domain.board.mapper;

import com.weady.weady.domain.board.dto.request.BoardCreateRequestDto;
import com.weady.weady.domain.board.dto.request.BoardPlaceRequestDto;
import com.weady.weady.domain.board.dto.response.*;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardImg;
import com.weady.weady.domain.board.entity.board.BoardPlace;
import com.weady.weady.domain.board.entity.board.BoardStyle;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    public static Board toBoard(BoardCreateRequestDto request, User user,
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

    public static List<BoardPlace> toBoardPlaceList(List<BoardPlaceRequestDto> requestDtos) {
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


    /// 응답 dto ///

    // 게시물 조회 dto
    public static BoardResponseDto toBoardResponseDto(Board board, User user) {
        List<Long> styleIdList = board.getBoardStyleList().stream()
                .map(style -> style.getClothesStyleCategory().getId())
                .collect(Collectors.toList());

        return BoardResponseDto.builder()
                .boardId(board.getId())
                .isPublic(board.getIsPublic())
                .content(board.getContent())
                .weatherTagId(board.getWeatherTag().getId())
                .temperatureTagId(board.getTemperatureTag().getId())
                .seasonTagId(board.getSeasonTag().getId())
                .userName(user.getName())
                .userProfileImageUrl(user.getProfileImgUrl())
                .likeCount(board.getGoodCount())
                .placeDtoList(toBoardPlaceResponseListDto(board.getBoardPlaceList()))
                .styleIdList(styleIdList)
                .createdAt(board.getCreatedAt())
                .build();
    }


    public static List<BoardPlaceResponseDto> toBoardPlaceResponseListDto(List<BoardPlace> places){
        return places.stream()
                .map(place -> BoardPlaceResponseDto.builder()
                        .placeName(place.getPlaceName())
                        .placeAddress(place.getPlaceAddress())
                        .build())
                .collect(Collectors.toList());
    }

    // 보드 홈 게시물 조회 리스트
    public static Slice<BoardHomeResponseDto> toBoardHomeResponseSliceDto(Slice<Board> boards) {
        return boards.map(BoardMapper::toBoardHomeResponseDto);

    }


    public static BoardHomeResponseDto toBoardHomeResponseDto(Board board) {
        // imgOrder == 1 인 이미지의 url 가져오기
        String firstOrderUrl = board.getBoardImg().stream()
                .filter(boardImg -> boardImg.getImgOrder() == 1)
                .map(BoardImg::getImgUrl)
                .findFirst().orElse(null);

        return BoardHomeResponseDto.builder()
                .boardID(board.getId())
                .imgUrl(firstOrderUrl)
                .userId(board.getUser().getId())
                .seasonTagId(board.getSeasonTag().getId())
                .temperatureTagId(board.getTemperatureTag().getId())
                .weatherTagId(board.getWeatherTag().getId())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
