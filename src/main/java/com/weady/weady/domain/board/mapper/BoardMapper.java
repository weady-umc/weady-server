package com.weady.weady.domain.board.mapper;

import com.weady.weady.domain.board.dto.BoardRequest;
import com.weady.weady.domain.board.entity.Board;
import com.weady.weady.domain.board.entity.BoardImg;
import com.weady.weady.domain.board.entity.BoardPlace;

import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    public static Board toBoard(BoardRequest.BoardCreateRequestDTO request){
        return Board.builder()
                .isPublic(request.isPublic())
                .content(request.content())
                .build();
    }


    public static List<BoardPlace> toBoardPlaceList(List<BoardRequest.BoardPlaceRequestDTO> requestDTOS, Board board) {
        return requestDTOS.stream()
                .map(dto -> BoardPlace.builder()
                        .placeName(dto.placeName())
                        .placeAddress(dto.placeAddress())
                        .board(board) //연관관계 주입
                        .build())
                .collect(Collectors.toList());
    }

}
