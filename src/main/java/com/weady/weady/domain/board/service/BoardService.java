package com.weady.weady.domain.board.service;

import com.weady.weady.domain.board.dto.request.BoardCreateRequestDto;
import com.weady.weady.domain.board.dto.response.BoardGoodResponseDto;
import com.weady.weady.domain.board.dto.response.BoardHomeResponseDto;
import com.weady.weady.domain.board.dto.response.BoardResponseDto;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardGood;
import com.weady.weady.domain.board.mapper.BoardMapper;
import com.weady.weady.domain.board.repository.BoardGoodRepository;
import com.weady.weady.domain.board.repository.BoardRepository;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.tags.repository.season.SeasonRepository;
import com.weady.weady.domain.tags.repository.clothesStyleCategory.ClothesStyleCategoryRepository;
import com.weady.weady.domain.tags.repository.temperature.TemperatureRepository;
import com.weady.weady.domain.tags.repository.weather.WeatherRepository;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.global.common.error.errorCode.BoardErrorCode;
import com.weady.weady.global.common.error.errorCode.TagsErrorCode;
import com.weady.weady.global.common.error.errorCode.UserErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import com.weady.weady.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardGoodRepository boardGoodRepository;
    private final UserRepository userRepository;
    private final SeasonRepository seasonRepository;
    private final TemperatureRepository temperatureRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesStyleCategoryRepository styleCategoryRepository;
    //private final S3Uploader s3Uploader;


    /**
     * 게시글 작성
     * @return BoardResponseDto
     * @thorws
     */
    @Transactional
    public BoardResponseDto createPost(BoardCreateRequestDto requestDto) {

        // 게시글 작성자 정보 조회
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        //날씨 태그 조회
        SeasonTag seasonTag = seasonRepository.findById(requestDto.seasonTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.SEASON_TAG_NOT_FOUND));

        TemperatureTag temperatureTag = temperatureRepository.findById(requestDto.temperatureTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.TEMPERATURE_TAG_NOT_FOUND));

        WeatherTag weatherTag = weatherRepository.findById(requestDto.weatherTagId())
                .orElseThrow(() -> new BusinessException(TagsErrorCode.WEATHER_TAG_NOT_FOUND));

        List<ClothesStyleCategory> categories = styleCategoryRepository.findAllById(requestDto.styleIds());

        // 데이터 저장
        Board board = BoardMapper.toBoard(requestDto, user, seasonTag, temperatureTag, weatherTag);
        board.updateBoardPlaceList(BoardMapper.toBoardPlaceList(requestDto.boardPlaceRequestDtoList()));
        board.updateBoardStyleList(BoardMapper.toBoardStyleList(categories));

        boardRepository.save(board);

        return BoardMapper.toBoardResponseDto(board, user, false);

    }

    /**
     * 게시글 수정
     * @return BoardResponseDto
     * @thorws
     */
    @Transactional
    public BoardResponseDto updatePost(BoardCreateRequestDto requestDto, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(()-> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        User boardUser = board.getUser(); // 작성자

        Long userId = SecurityUtil.getCurrentUserId(); // 현재 로그인 한 사용자

        if (!userId.equals(boardUser.getId())){
            throw new BusinessException(BoardErrorCode.UNAUTHORIZED_UPDATE);
        }

        //날씨 태그 조회
        SeasonTag seasonTag = seasonRepository.findById(requestDto.seasonTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.SEASON_TAG_NOT_FOUND));

        TemperatureTag temperatureTag = temperatureRepository.findById(requestDto.temperatureTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.TEMPERATURE_TAG_NOT_FOUND));

        WeatherTag weatherTag = weatherRepository.findById(requestDto.weatherTagId())
                .orElseThrow(() -> new BusinessException(TagsErrorCode.WEATHER_TAG_NOT_FOUND));

        List<ClothesStyleCategory> categories = styleCategoryRepository.findAllById(requestDto.styleIds());

        // 변경 사항 업데이트
        board.updateBoard(requestDto.isPublic(), requestDto.content(), seasonTag, temperatureTag, weatherTag);
        board.updateBoardPlaceList(BoardMapper.toBoardPlaceList(requestDto.boardPlaceRequestDtoList()));
        board.updateBoardStyleList(BoardMapper.toBoardStyleList(categories));

        boolean goodStatus = boardGoodRepository.existsByBoardAndUser(board, boardUser);

        return BoardMapper.toBoardResponseDto(board, boardUser, goodStatus);

    }

    /**
     * 게시글 삭제
     * @return
     * @thorws
     */
    @Transactional
    public void deletePost(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()-> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(()-> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (board.getUser().equals(user)) {
            boardRepository.delete(board);
            return;
        }
        throw new BusinessException(BoardErrorCode.UNAUTHORIZED_DELETE);
    }


    /**
     * 보드 홈 - 전체 게시글 조회
     * @return BoardHomeResponseListDto
     * @thorws
     */
    @Transactional(readOnly = true)
    public Slice<BoardHomeResponseDto> getFilteredAndSortedBoards(Long seasonTagId, Long temperatureTagId, Long weatherTagId, Integer size) {

        Pageable pageable = PageRequest.of(0, size);
        Slice<Board> boards = boardRepository.getFilteredAndSortedResults(seasonTagId, temperatureTagId, weatherTagId, pageable);

        return BoardMapper.toBoardHomeResponseSliceDto(boards);
    }


    /**
     * 특정 게시글 조회
     * @return BoardResponseDto
     * @thorws
     */
    public BoardResponseDto getPostById(Long id) {

        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        boolean goodStatus = boardGoodRepository.existsByBoardAndUser(board, user);

        return BoardMapper.toBoardResponseDto(board, user, goodStatus);
    }

    /**
     * 게시글 좋아요
     * @return BoardGoodResponseDto
     * @thorws
     */
    @Transactional
    public BoardGoodResponseDto addGood(Long boardId) {

        // 좋아요 누르는 유저 정보
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        if (boardGoodRepository.existsByBoardAndUser(board, user)) { //이미 존재하는 경우
            throw new BusinessException(BoardErrorCode.ALREADY_LIKED);
        }

        BoardGood boardGood = BoardMapper.toBoardGood(board, user);
        boardGoodRepository.save(boardGood);

        board.increaseGoodCount();
        Integer goodCount = board.getGoodCount();

        return BoardMapper.toBoardGoodResponseDto(true, goodCount);


    }

    /**
     * 게시글 좋아요 취소
     * @return BoardGoodResponseDto
     * @thorws
     */
    @Transactional
    public BoardGoodResponseDto cancelGood(Long boardId){

        // 좋아요 누르는 유저 정보
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        boardGoodRepository.deleteByBoardAndUser(board, user);

        board.decreaseGoodCount();

        Integer goodCount = board.getGoodCount();

        return BoardMapper.toBoardGoodResponseDto(false, goodCount);

    }
}
