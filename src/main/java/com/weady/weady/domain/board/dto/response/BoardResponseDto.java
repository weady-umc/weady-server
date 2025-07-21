package com.weady.weady.domain.board.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

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
