package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

@Builder
public record PageInfoDto(
        Long nextCursor,
        Integer size,
        boolean hasNext
){}
