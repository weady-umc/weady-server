package com.weady.weady.domain.user.service;

import com.weady.weady.common.error.errorCode.BoardErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardImg;
import com.weady.weady.domain.board.repository.BoardImgRepository;
import com.weady.weady.domain.board.repository.BoardRepository;
import com.weady.weady.domain.user.dto.response.GetBoardInMyPageResponse;
import com.weady.weady.domain.user.dto.response.GetMyPageResponse;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.mapper.MyPageMapper;
import com.weady.weady.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardImgRepository boardImgRepository;

    @Transactional(readOnly = true)
    public GetMyPageResponse getMyPage(int year, int month) {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        List<Board> boards = boardRepository.findBoardsByUserIdAndYearMonth(userId, year, month);

        Map<LocalDate, Board> firstBoardByDate = boards.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedAt().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(Board::getCreatedAt)),
                                Optional::get
                        )
                ));

        List<GetMyPageResponse.CalendarResponse> calendar = firstBoardByDate.entrySet().stream()
                .map(entry -> {
                    Board board = entry.getValue();
                    String thumbnail = boardImgRepository
                            .findThumbnailUrlByBoardId(board.getId())
                            .orElse(null);
                    Long weatherTagId = (board.getWeatherTag() != null) ? board.getWeatherTag().getId() : null;
                    return MyPageMapper.toCalendarResponse(entry.getKey(), thumbnail, weatherTagId, board.getIsPublic());
                })
                .toList();

        return MyPageMapper.toGetMyPageResponse(user, calendar);
    }

    @Transactional(readOnly = true)
    public GetBoardInMyPageResponse getBoardInMyPage(LocalDate date, boolean isPublic) {
        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        Board board = boardRepository.findBoardByUserIdAndCreatedAtBetweenAndIsPublic(userId, start, end, isPublic)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        List<BoardImg> images = boardImgRepository.findAllByBoardIdOrderByImgOrderAsc(board.getId());

        return MyPageMapper.toGetBoardInMyPageResponse(board, images);
    }
}
