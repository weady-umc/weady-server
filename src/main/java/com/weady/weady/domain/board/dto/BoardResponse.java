package com.weady.weady.domain.board.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class BoardResponse {

    // 게시물 조회
    @Builder
    public record BoardResponseDto(
            Long boardId,
            String userName,
            String userProfileImageUrl,
            Boolean isPublic,
            Integer likeCount,
            //List<BoardImgResponseDTO> imageDtoList,
            String content,

            Long weatherTagId,
            Long temperatureTagId,
            Long seasonTagId,

            List<BoardPlaceResponseDto> placeDtoList,
            List<BoardStyleResponseDto> styleIdList,
            // List<Long> brandId,

            LocalDateTime createdAt
    ){}

    // 보드 홈 화면 조회용
    @Builder
    public record BoardHomeResponseDto(
            Long boardID,
            Long userId,
            String imgUrl, //대표 사진 url

            Long weatherTagId,
            Long seasonTagId,

            LocalDateTime createdAt
    ){}

    @Builder
    public record BoardHomeResponseSliceListDto(
            List<BoardHomeResponseDto> boardHomeResponseDTOList,
            PageInfoDto pageInfoDto
    ){}

    @Builder
    public record PageInfoDto(
            Long nextCursor,
            Long size,
            boolean hasNext
    ){}
        @Builder
    public record BoardImgResponseDto(
            String imgUrl,
            Integer imgOrder
    ){}

    @Builder
    public record BoardPlaceResponseDto(
            String placeName,
            String placeAddress
    ){}

    @Builder
    public record BoardStyleResponseDto(
            Long styleTagId
    ){}

}
