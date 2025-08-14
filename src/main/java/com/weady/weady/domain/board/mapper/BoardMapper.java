package com.weady.weady.domain.board.mapper;

import com.weady.weady.domain.board.dto.request.BoardBrandRequestDto;
import com.weady.weady.domain.board.dto.request.BoardCreateRequestDto;
import com.weady.weady.domain.board.dto.request.BoardPlaceRequestDto;
import com.weady.weady.domain.board.dto.response.*;
import com.weady.weady.domain.board.entity.board.*;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardMapper {

    public static Board toBoard(BoardCreateRequestDto request, User user,
                                SeasonTag seasonTag, TemperatureTag temperatureTag, WeatherTag weatherTag, Integer imgCount) {
        return Board.builder()
                .isPublic(request.isPublic())
                .content(request.content())
                .seasonTag(seasonTag)
                .temperatureTag(temperatureTag)
                .weatherTag(weatherTag)
                .imgCount(imgCount)
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

    public static List<BoardImg> toBoardImgList(List<String> imgUrls, Board board) {
        return IntStream.range(0, imgUrls.size())
                .mapToObj(i -> BoardImg.builder()
                        .board(board)
                        .imgOrder(i+1)  // 순서는 1부터 시작!
                        .imgUrl(imgUrls.get(i))
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

    public static List<BoardBrand> toBoardBrandList(List<BoardBrandRequestDto> requestDtos) {
        return requestDtos.stream()
                .map(dto -> BoardBrand.builder()
                        .brand(dto.brand())
                        .product(dto.product())
                        .build())
                .collect(Collectors.toList());
    }

    public static BoardGood toBoardGood(Board board, User user) {
        return BoardGood.builder()
                .board(board)
                .user(user)
                .build();
    }

    public static BoardHidden toBoardHidden(Board board, User user) {
        return BoardHidden.builder()
                .board(board)
                .user(user)
                .build();
    }

    public static Report toReport(Board board, User user, ReportType reportType, String content) {
        return Report.builder()
                .board(board)
                .user(user)
                .reportType(reportType)
                .content(content)
                .build();
    }




    /// 응답 dto ///

    // 게시물 조회 dto
    public static BoardResponseDto toBoardResponseDto(Board board, User boardUser, boolean goodStatus) {
        List<Long> styleIdList = board.getBoardStyleList().stream()
                .map(style -> style.getClothesStyleCategory().getId())
                .collect(Collectors.toList());

        return BoardResponseDto.builder()
                .boardId(board.getId())
                .userId(boardUser.getId())
                .userName(boardUser.getName())
                .userProfileImageUrl(boardUser.getProfileImageUrl())

                .isPublic(board.getIsPublic()) // 게시 여부
                .goodStatus(goodStatus) // 현재 로그인 한 사용자가 해당 게시물에 좋아요를 눌렀는지 여부
                .goodCount(board.getGoodCount())    // 좋아요 개수
                .commentCount(board.getCommentCount())  // 댓글 개수
                .content(board.getContent()) // 내용

                .weatherTagId(board.getWeatherTag().getId())
                .temperatureTagId(board.getTemperatureTag().getId())
                .seasonTagId(board.getSeasonTag().getId())

                .placeDtoList(toBoardPlaceResponseListDto(board.getBoardPlaceList())) //장소
                .imgCount(board.getImgCount())  // 이미지 개수
                .imageDtoList(toBoardImgResponseListDto(board.getBoardImgList())) // 이미지
                .styleIdList(styleIdList)  // 스타일 태그
                .brandDtoList(toBoardBrandResponseListDto(board.getBoardBrandList())) // 옷 정보
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
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


    public static List<BoardImgResponseDto> toBoardImgResponseListDto(List<BoardImg> images){
        return images.stream()
                .map(image -> BoardImgResponseDto.builder()
                        .imgOrder(image.getImgOrder())
                        .imgUrl(image.getImgUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<BoardBrandResponseDto> toBoardBrandResponseListDto(List<BoardBrand> brands){
        return brands.stream()
                .map(brand -> BoardBrandResponseDto.builder()
                        .brand(brand.getBrand())
                        .product(brand.getProduct())
                        .build())
                .collect(Collectors.toList());
    }


    // 보드 홈 게시물 조회 리스트
    public static Slice<BoardHomeResponseDto> toBoardHomeResponseSliceDto(Slice<Board> boards) {
        return boards.map(BoardMapper::toBoardHomeResponseDto);

    }


    public static BoardHomeResponseDto toBoardHomeResponseDto(Board board) {
        // imgOrder == 1 인 이미지의 url 가져오기
        String firstOrderUrl = board.getBoardImgList().stream()
                .filter(boardImg -> boardImg.getImgOrder() == 1)
                .map(BoardImg::getImgUrl)
                .findFirst().orElse(null);

        return BoardHomeResponseDto.builder()
                .boardId(board.getId())
                .imgUrl(firstOrderUrl)
                .userId(board.getUser().getId())
                .seasonTagId(board.getSeasonTag().getId())
                .temperatureTagId(board.getTemperatureTag().getId())
                .weatherTagId(board.getWeatherTag().getId())
                .createdAt(board.getCreatedAt())
                .build();
    }


    public static BoardGoodResponseDto toBoardGoodResponseDto(Boolean goodStatus, Integer goodCount) {
        return BoardGoodResponseDto.builder()
                .goodStatus(goodStatus)
                .goodCount(goodCount)
                .build();
    }
}
