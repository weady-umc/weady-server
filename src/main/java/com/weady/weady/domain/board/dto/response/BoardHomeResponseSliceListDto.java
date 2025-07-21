package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BoardHomeResponseSliceListDto(
        List<BoardHomeResponseDto> boardHomeResponseDTOList,
        PageInfoDto pageInfoDto
){}