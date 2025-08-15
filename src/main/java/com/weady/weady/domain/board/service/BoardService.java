package com.weady.weady.domain.board.service;

import com.weady.weady.common.external.s3.S3Uploader;
import com.weady.weady.domain.board.dto.request.BoardCreateRequestDto;
import com.weady.weady.domain.board.dto.request.ReportRequestDto;
import com.weady.weady.domain.board.dto.response.BoardGoodResponseDto;
import com.weady.weady.domain.board.dto.response.BoardHomeResponseDto;
import com.weady.weady.domain.board.dto.response.BoardResponseDto;
import com.weady.weady.domain.board.entity.board.*;
import com.weady.weady.domain.board.mapper.BoardMapper;
import com.weady.weady.domain.board.repository.BoardGoodRepository;
import com.weady.weady.domain.board.repository.BoardHiddenRepository;
import com.weady.weady.domain.board.repository.BoardRepository;
import com.weady.weady.domain.board.repository.ReportRepository;
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
import com.weady.weady.common.error.errorCode.BoardErrorCode;
import com.weady.weady.common.error.errorCode.TagsErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import com.weady.weady.domain.user.service.UserFavoriteLocationService;
import com.weady.weady.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardGoodRepository boardGoodRepository;
    private final BoardHiddenRepository boardHiddenRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SeasonRepository seasonRepository;
    private final TemperatureRepository temperatureRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesStyleCategoryRepository styleCategoryRepository;
    private final S3Uploader s3Uploader;

    /**
     * 1. 보드 홈 - 전체 게시글 조회
     * @return BoardHomeResponseListDto
     * @throws
     */
    @Transactional(readOnly = true)
    public Slice<BoardHomeResponseDto> getFilteredAndSortedBoards(Long seasonTagId, Long temperatureTagId, Long weatherTagId, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(0, size);
        Slice<Board> boards = boardRepository.getFilteredAndSortedResults(seasonTagId, temperatureTagId, weatherTagId, userId, pageable);

        return BoardMapper.toBoardHomeResponseSliceDto(boards);
    }


    /**
     * 2. 특정 게시글 조회
     * @return BoardResponseDto
     * @throws
     */
    @Transactional(readOnly = true)
    public BoardResponseDto getPostById(Long boardId) {

        Board board = getBoardById(boardId);
        User user = getAuthenticatedUser();

        boolean goodStatus = boardGoodRepository.existsByBoardAndUser(board, user);

        User boardUser = board.getUser();

        return BoardMapper.toBoardResponseDto(board, boardUser, goodStatus);
    }

    /**
     * 3. 게시글 작성
     * @return BoardResponseDto
     * @throws
     */
    @Transactional
    public BoardResponseDto createPost(List<MultipartFile> images, BoardCreateRequestDto postData) {

        User user = getAuthenticatedUser();

        // 날씨 태그 조회
        SeasonTag seasonTag = seasonRepository.findById(postData.seasonTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.SEASON_TAG_NOT_FOUND));

        TemperatureTag temperatureTag = temperatureRepository.findById(postData.temperatureTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.TEMPERATURE_TAG_NOT_FOUND));

        WeatherTag weatherTag = weatherRepository.findById(postData.weatherTagId())
                .orElseThrow(() -> new BusinessException(TagsErrorCode.WEATHER_TAG_NOT_FOUND));

        List<ClothesStyleCategory> categories = styleCategoryRepository.findAllById(postData.styleIds());

        // 이미지 업로드
        List<String> imageUrls = images.stream()
                .map(image -> s3Uploader.upload(image, "board"))
                .collect(Collectors.toList());


        // 데이터 저장
        Board board = BoardMapper.toBoard(postData, user, seasonTag, temperatureTag, weatherTag, images.size());
        board.updateBoardPlaceList(BoardMapper.toBoardPlaceList(postData.boardPlaceRequestDtoList()));
        board.updateBoardStyleList(BoardMapper.toBoardStyleList(categories));
        board.updateBoardImgList(BoardMapper.toBoardImgList(imageUrls, board));
        board.updateBoardBrandList(BoardMapper.toBoardBrandList(postData.boardBrandRequestDtoList()));

        boardRepository.save(board);

        return BoardMapper.toBoardResponseDto(board, user, false);

    }

    /**
     * 4. 게시글 수정
     * @return BoardResponseDto
     * @throws
     */
    @Transactional
    public BoardResponseDto updatePost(BoardCreateRequestDto requestDto, Long boardId) {

        Board board = getBoardById(boardId);
        User boardUser = board.getUser(); // 작성자

        User user = getAuthenticatedUser();

        if (!user.equals(boardUser)) {
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
        board.updateBoardBrandList(BoardMapper.toBoardBrandList(requestDto.boardBrandRequestDtoList()));

        boolean goodStatus = boardGoodRepository.existsByBoardAndUser(board, boardUser);

        return BoardMapper.toBoardResponseDto(board, boardUser, goodStatus);

    }

    /**
     * 5. 게시글 삭제
     * @throws BoardErrorCode.UNAUTHORIZED_DELETE : 게시글 작성자와 현재 로그인 한 사용자가 다른 경우 예외
     */
    @Transactional
    public void deletePost(Long boardId) {

        Board board = getBoardById(boardId);

        User boardUser = board.getUser();
        User user = getAuthenticatedUser();

        if (boardUser.equals(user)) {
            boardRepository.delete(board);
            return;
        }
        throw new BusinessException(BoardErrorCode.UNAUTHORIZED_DELETE);
    }



    /**
     * 8. 게시글 좋아요
     * @return BoardGoodResponseDto
     * @throws BoardErrorCode.ALREADY_LIKED
     */
    @Transactional
    public BoardGoodResponseDto addGood(Long boardId) {

        // 좋아요 누르는 유저 정보
        User user = getAuthenticatedUser();

        Board board = getBoardById(boardId);

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
     * 9. 게시글 좋아요 취소
     * @return BoardGoodResponseDto
     * @throws BoardErrorCode.BOARD_GOOD_NOT_FOUND
     */
    @Transactional
    public BoardGoodResponseDto cancelGood(Long boardId){

        // 좋아요 누르는 유저 정보
        User user = getAuthenticatedUser();

        Board board = getBoardById(boardId);

        if (!boardGoodRepository.existsByBoardAndUser(board, user)) { //데이터가 존재하지 않는 경우
            throw new BusinessException(BoardErrorCode.BOARD_GOOD_NOT_FOUND);
        }
        boardGoodRepository.deleteByBoardAndUser(board, user);

        board.decreaseGoodCount();

        Integer goodCount = board.getGoodCount();

        return BoardMapper.toBoardGoodResponseDto(false, goodCount);

    }

    /**
     * 10. 게시물 신고
     * @throws BoardErrorCode.ALREADY_REPORTED
     */
    @Transactional
    public void reportPost(Long boardId, ReportRequestDto requestDto) {

        // 신고하는 유저 정보
        User user = getAuthenticatedUser();

        Board board = getBoardById(boardId);

        ReportType reportType = requestDto.reportType();

        if (reportRepository.existsByBoardAndUser(board, user)) { //이미 신고 한 경우
            throw new BusinessException(BoardErrorCode.ALREADY_REPORTED);
        }

        Report report = BoardMapper.toReport(board, user, reportType, requestDto.content());
        reportRepository.save(report);

        // 게시물 신고 시, 신고자에게는 해당 게시물 숨김 처리
        hidePost(boardId);

    }


    /**
     * 11. 게시글 숨김
     * @throws BoardErrorCode.ALREADY_HIDDEN
     */
    @Transactional
    public void hidePost(Long boardId) {

        User user = getAuthenticatedUser();

        Board board = getBoardById(boardId);

        if (boardHiddenRepository.existsByBoardAndUser(board, user)) { //이미 존재하는 경우
            throw new BusinessException(BoardErrorCode.ALREADY_HIDDEN);
        }

        BoardHidden boardHidden = BoardMapper.toBoardHidden(board, user);
        boardHiddenRepository.save(boardHidden);

    }

    /**
     * 12. 게시글 숨김 취소
     * @throws BoardErrorCode.BOARD_HIDDEN_NOT_FOUND
     */
    @Transactional
    public void unhidePost(Long boardId){

        User user = getAuthenticatedUser();

        Board board = getBoardById(boardId);

        if (!boardHiddenRepository.existsByBoardAndUser(board, user)) { //데이터가 존재하지 않는 경우
            throw new BusinessException(BoardErrorCode.BOARD_HIDDEN_NOT_FOUND);
        }
        boardHiddenRepository.deleteByBoardAndUser(board, user);
    }


    private Board getBoardById(Long boardId) {
        return boardRepository.findByBoardId(boardId)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));
    }

    private User getAuthenticatedUser() {
        return userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    }

}

