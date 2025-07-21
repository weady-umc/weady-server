package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

@Builder
public record PageInfoDto(
        Long cursor,
        Long size,
        boolean hasNext
){}
