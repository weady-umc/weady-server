package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

// 보드 홈 화면 조회용
@Builder
public record BoardHomeResponseDto(
        Long boardId,
        Long userId,
        String imgUrl, //대표 사진 url

        Long weatherTagId,
        Long temperatureTagId,
        Long seasonTagId,

        LocalDateTime createdAt
){}
