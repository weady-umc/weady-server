package com.weady.weady.domain.weadychive.dto.board.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ScrapBoardResponseDto(
        Boolean isScrapped) {}
