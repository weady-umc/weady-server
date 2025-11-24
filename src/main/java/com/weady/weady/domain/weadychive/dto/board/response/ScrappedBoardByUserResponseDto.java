package com.weady.weady.domain.weadychive.dto.board.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ScrappedBoardByUserResponseDto(
        String username,
        Long boardId,
        String imgUrl, //대표 사진 url

        Long weatherTagId) {
}
