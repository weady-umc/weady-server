package com.weady.weady.domain.board.service;

import com.weady.weady.domain.board.dto.BoardRequest;
import com.weady.weady.domain.board.dto.BoardResponse;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.mapper.BoardMapper;
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
import com.weady.weady.domain.user.service.ExampleUserService;
import com.weady.weady.global.common.error.errorCode.TagsErrorCode;
import com.weady.weady.global.common.error.errorCode.UserErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import com.weady.weady.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final SeasonRepository seasonRepository;
    private final TemperatureRepository temperatureRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesStyleCategoryRepository styleCategoryRepository;
    private final ExampleUserService exampleUserService;
    //private final S3Uploader s3Uploader;


    /**
     * 게시글 작성
     * @return BoardResponseDto
     * @thorws
     */
    @Transactional
    public BoardResponse.BoardResponseDto createPost(BoardRequest.BoardCreateRequestDto requestDto) {

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

        return BoardMapper.toBoardResponseDto(board, user);


    }

}
