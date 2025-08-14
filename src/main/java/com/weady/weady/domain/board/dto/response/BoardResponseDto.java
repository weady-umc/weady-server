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
        Integer commentCount,
        Integer imgCount,
        List<BoardImgResponseDto> imageDtoList,
        String content,


        Long seasonTagId,  // 계절
        Long temperatureTagId,  // 기온
        Long weatherTagId,  // 날씨

        List<BoardPlaceResponseDto> placeDtoList,
        List<Long> styleIdList,
        List<BoardBrandResponseDto> brandDtoList,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}
