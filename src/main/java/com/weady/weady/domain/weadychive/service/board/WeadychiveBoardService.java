package com.weady.weady.domain.weadychive.service.board;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.repository.BoardRepository;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.domain.weadychive.dto.board.request.ScrapBoardRequestDto;
import com.weady.weady.domain.weadychive.dto.board.response.ScrapBoardResponseDto;
import com.weady.weady.domain.weadychive.dto.board.response.ScrappedBoardByUserResponseDto;
import com.weady.weady.domain.weadychive.entity.WeadychiveBoard;
import com.weady.weady.domain.weadychive.mapper.board.WeadychiveBoardMapper;
import com.weady.weady.domain.weadychive.repository.board.WeadychiveBoardRepository;
import com.weady.weady.common.error.errorCode.BoardErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class WeadychiveBoardService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final WeadychiveBoardRepository weadychiveBoardRepository;


    /**
     * 스크랩한 게시물 가져오기
     * @return Slice<scrappedBoardByUserResponseDto>
     * @throws ...
     */
    @Transactional(readOnly = true)
    public Slice<ScrappedBoardByUserResponseDto> getScrappedBoard(Integer size){

        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, size);
        Slice<Board> boards = weadychiveBoardRepository.findBoardsScrappedByUserId(currentUserId ,pageable);

        return WeadychiveBoardMapper.toScrappedBoardByUserResponseSliceDto(user, boards);

    }


    /**
     * 게시물 스크랩하기
     * @return ScrapBoardResponseDto
     * @throws ...
     */
    public ScrapBoardResponseDto scrapBoard(ScrapBoardRequestDto requestDto){
        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(requestDto.boardId())
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        if (weadychiveBoardRepository.existsByUserAndBoard(user, board)) { //이미 저장한 경우
            throw new BusinessException(BoardErrorCode.ALREADY_SCRAPED);
        }

        WeadychiveBoard weadychiveBoard = WeadychiveBoardMapper.toEntity(user, board);
        weadychiveBoardRepository.save(weadychiveBoard);

        return WeadychiveBoardMapper.toScrapBoardResponseDto(true);
    }


    /**
     * 게시물 스크랩 취소하기
     * @return ScrapBoardResponseDto
     * @throws ...
     */
    @Transactional
    public ScrapBoardResponseDto cancelScrapBoard(ScrapBoardRequestDto requestDto){
        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(requestDto.boardId())
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        if (!weadychiveBoardRepository.existsByUserAndBoard(user, board)) { // 저장 내역이 존재하지 않는 경우
            throw new BusinessException(BoardErrorCode.WEADYCHIVE_BOARD_NOT_FOUND);
        }

        weadychiveBoardRepository.deleteByUserAndBoard(user, board);

        return WeadychiveBoardMapper.toScrapBoardResponseDto(false);

    }
}
