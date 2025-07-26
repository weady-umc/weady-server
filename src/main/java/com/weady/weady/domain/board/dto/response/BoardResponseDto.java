package com.weady.weady.domain.board.dto.response;

import com.weady.weady.domain.board.entity.board.BoardStyle;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

// 게시물 조회
@Builder
public record BoardResponseDto(
        Long boardId,
        Long userId,
        String userName,
        String userProfileImageUrl,
        Boolean isPublic,
        Boolean goodStatus, //게시글을 조회하는 사용자가 좋아요를 눌렀으면 true
        Integer goodCount,
        //List<BoardImgResponseDTO> imageDtoList,
        String content,

        Long weatherTagId,
        Long temperatureTagId,
        Long seasonTagId,

        List<BoardPlaceResponseDto> placeDtoList,
        List<Long> styleIdList,
        // List<Long> brandId,

        LocalDateTime createdAt
){}
