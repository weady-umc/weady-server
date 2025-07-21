package com.weady.weady.domain.board.mapper;

import com.weady.weady.domain.board.dto.BoardResponse;
import com.weady.weady.domain.board.dto.request.BoardCreateRequestDto;
import com.weady.weady.domain.board.dto.request.BoardPlaceRequestDto;
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

    // 보드 홈 게시물 조회 리스트
    public static BoardResponse.BoardHomeResponseListDto toBoardHomeResponseListDto(Slice<Board> boards) {

        List<BoardResponse.BoardHomeResponseDto> boardHomeResponseDtos = boards.getContent().stream()
                .map( board -> {

                    String firstOrder = board.getBoardImg().stream()
                            .filter(boardImg -> boardImg.getImgOrder() == 1)
                            .map(BoardImg::getImgUrl)
                            .findFirst().orElse(null);

                    return BoardResponse.BoardHomeResponseDto.builder()
                            .boardID(board.getId())
                            .imgUrl(firstOrder)
                            .userId(board.getUser().getId())
                            .seasonTagId(board.getSeasonTag().getId())
                            .weatherTagId(board.getWeatherTag().getId())
                            .createdAt(board.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        List<Board> content = boards.getContent();
        Long nextCursor = content.isEmpty() ? null : content.get(content.size() - 1).getId();

        BoardResponse.PageInfoDto pageInfoDto = BoardResponse.PageInfoDto.builder()
                .cursor(nextCursor)
                .hasNext(boards.hasNext())
                .build();

        return BoardResponse.BoardHomeResponseListDto.builder()
                .boardHomeResponseDTOList(boardHomeResponseDtos)
                .pageInfoDto(pageInfoDto)
                .build();
    }
}
