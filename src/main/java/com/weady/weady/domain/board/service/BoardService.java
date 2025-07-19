package com.weady.weady.domain.board.service;

import com.weady.weady.domain.board.dto.BoardRequest;
import com.weady.weady.domain.board.dto.BoardResponse;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardPlace;
import com.weady.weady.domain.board.entity.board.BoardStyle;
import com.weady.weady.domain.board.mapper.BoardMapper;
import com.weady.weady.domain.board.repository.board.BoardRepository;
import com.weady.weady.domain.board.repository.boardPlace.BoardPlaceRepository;
import com.weady.weady.domain.board.repository.boardStyle.BoardStyleRepository;
import com.weady.weady.domain.board.repository.styleCategory.StyleCategoryRepository;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.tags.repository.season.SeasonRepository;
import com.weady.weady.domain.tags.repository.temperature.TemperatureRepository;
import com.weady.weady.domain.tags.repository.weather.WeatherRepository;
import com.weady.weady.domain.user.dto.ExampleUserResponse;
import com.weady.weady.domain.user.service.ExampleUserService;
import com.weady.weady.global.common.error.errorCode.TagsErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardPlaceRepository boardPlaceRepository;
    private final BoardStyleRepository boardStyleRepository;
    private final SeasonRepository seasonRepository;
    private final TemperatureRepository temperatureRepository;
    private final WeatherRepository weatherRepository;
    private final StyleCategoryRepository styleCategoryRepository;
    private final ExampleUserService exampleUserService;
    //private final S3Uploader s3Uploader;


    /**
     * 게시글 작성
     * @return BoardResponseDto
     * @thorws
     */
    public BoardResponse.BoardResponseDto createPost(BoardRequest.BoardCreateRequestDto requestDto) {

        // 게시글 작성자 정보 조회
        ExampleUserResponse.ExampleUserResponseDto userResponseDto = exampleUserService.getUser();

        //날씨 태그 조회
        SeasonTag seasonTag = seasonRepository.findById(requestDto.seasonTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.SEASON_TAG_NOT_FOUND));

        TemperatureTag temperatureTag = temperatureRepository.findById(requestDto.temperatureTagId())
                .orElseThrow(()-> new BusinessException(TagsErrorCode.TEMPERATURE_TAG_NOT_FOUND));

        WeatherTag weatherTag = weatherRepository.findById(requestDto.weatherTagId())
                .orElseThrow(() -> new BusinessException(TagsErrorCode.WEATHER_TAG_NOT_FOUND));

        List<ClothesStyleCategory> categories = styleCategoryRepository.findAllById(requestDto.styleId());


        // 데이터 저장
        Board board = BoardMapper.toBoard(requestDto, seasonTag, temperatureTag, weatherTag);
        List<BoardPlace> places = BoardMapper.toBoardPlaceList(requestDto.boardPlaceRequestDtoList(), board);
        List<BoardStyle> styles = BoardMapper.toBoardStyleList(categories, board);

        boardRepository.save(board);
        boardPlaceRepository.saveAll(places);
        boardStyleRepository.saveAll(styles);


        // 좋아요 개수 - 게시글 좋아요 기능 구현 후 수정 예정입니다!!
        int likeCount = 0;

        List<BoardResponse.BoardPlaceResponseDto> placeResponseDtos = BoardMapper.toBoardPlaceResponseListDto(places);
        List<BoardResponse.BoardStyleResponseDto> styleResponseDtos = BoardMapper.toBoardStyleResponseListDto(styles);

        return BoardMapper.toBoardResponseDto(board, placeResponseDtos, styleResponseDtos,userResponseDto, likeCount);


    }

}
