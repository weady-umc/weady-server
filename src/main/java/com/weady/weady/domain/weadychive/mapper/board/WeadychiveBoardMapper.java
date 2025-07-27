package com.weady.weady.domain.weadychive.mapper.board;

import com.weady.weady.domain.board.dto.response.BoardHomeResponseDto;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardImg;
import com.weady.weady.domain.board.mapper.BoardMapper;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.weadychive.dto.board.response.ScrapBoardResponseDto;
import com.weady.weady.domain.weadychive.dto.board.response.ScrappedBoardByUserResponseDto;
import com.weady.weady.domain.weadychive.entity.WeadychiveBoard;
import org.springframework.data.domain.Slice;

import java.util.List;

public class WeadychiveBoardMapper {

    public static WeadychiveBoard toEntity(User user, Board board){
        return WeadychiveBoard.builder()
                .user(user)
                .board(board)
                .build();
    }


    public static ScrapBoardResponseDto toScrapBoardResponseDto(Boolean isScraped) {
        return ScrapBoardResponseDto.builder()
                .isScraped(isScraped)
                .build();
    }


    public static Slice<ScrappedBoardByUserResponseDto> toScrappedBoardByUserResponseSliceDto(User user, Slice<Board> Boards) {
        return Boards.map(board->toScrappedBoardByUserResponseDto(user, board));

    }

    public static ScrappedBoardByUserResponseDto toScrappedBoardByUserResponseDto(User user, Board board) {
        // imgOrder == 1 인 이미지의 url 가져오기
        String firstOrderUrl = board.getBoardImgList().stream()
                .filter(boardImg -> boardImg.getImgOrder() == 1)
                .map(BoardImg::getImgUrl)
                .findFirst().orElse(null);

        return ScrappedBoardByUserResponseDto.builder()
                .username(user.getName())
                .boardId(board.getId())
                .weatherTagId(board.getWeatherTag().getId())
                .imgUrl(firstOrderUrl)
                .build();
    }

}
