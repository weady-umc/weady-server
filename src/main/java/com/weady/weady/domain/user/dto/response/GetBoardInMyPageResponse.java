package com.weady.weady.domain.user.dto.response;

import com.weady.weady.domain.board.dto.response.BoardImgResponseDto;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetBoardInMyPageResponse(Long boardId,
                                       LocalDateTime createdAt,
                                       Boolean isPublic,
                                       List<BoardImgResponseDto> imageList
                                       ) {
}
