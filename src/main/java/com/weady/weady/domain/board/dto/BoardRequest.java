package com.weady.weady.domain.board.dto;

import lombok.Builder;

import java.util.List;

public class BoardRequest {

    @Builder
    public record BoardCreateRequestDto ( // 이미지 파일 제외 postData 에 해당하는 데이터만.
            Boolean isPublic,
            String content,

            Long weatherTagId,
            Long temperatureTagId,
            Long seasonTagId,

            List<BoardRequest.BoardPlaceRequestDto> boardPlaceRequestDtoList,
            List<Long> styleIds
            //List<Long> brandId

            ) {}

    @Builder
    public record BoardPlaceRequestDto (
            String placeName,
            String placeAddress
    ){}
}
