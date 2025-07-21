package com.weady.weady.domain.board.dto.response;


import com.weady.weady.domain.board.dto.BoardResponse;
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

        List<BoardResponse.BoardPlaceResponseDto> placeDtoList,
        List<BoardResponse.BoardStyleResponseDto> styleIdList,
        // List<Long> brandId,

        LocalDateTime createdAt
){}
