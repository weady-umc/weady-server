package com.weady.weady.domain.board.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class BoardResponse {

    @Builder
    public record BoardResponseDTO(
            Long boardId,
            String username,
            String profileImgUrl,
            Boolean isPublic,
            Integer likeCount,
            List<BoardImgResponseDTO> imageDTOList,
            String content,

            // 태그 엔티티 정해지면 수정하기!
            //Long weatherTagId,
            //Long temperatureTagId,
            //Long seasonTagId,

            List<BoardPlaceResponseDTO> placeDTOList,
            //List<Long> styleId,
            // List<Long> brandId,

            LocalDateTime createdAt
    ){}

    @Builder
    public record BoardResponseListDTO(
            List<BoardResponseDTO> boardResponseDtoList
    ){}

    @Builder
    public record BoardImgResponseDTO(
            String imgUrl,
            Integer imgOrder
    ){}

    @Builder
    public record BoardPlaceResponseDTO(
            String placeName,
            String placeAddress
    ){}


}
