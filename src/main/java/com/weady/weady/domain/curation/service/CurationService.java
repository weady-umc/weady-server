package com.weady.weady.domain.curation.service;


import com.weady.weady.domain.curation.dto.Response.CurationByCurationIdResponseDto;
import com.weady.weady.domain.curation.dto.Response.CurationByLocationResponseDto;
import com.weady.weady.domain.curation.dto.Response.CurationCategoryResponseDto;
import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.curation.entity.CurationImg;
import com.weady.weady.domain.curation.mapper.CurationCategoryMapper;
import com.weady.weady.domain.curation.mapper.CurationMapper;
import com.weady.weady.domain.curation.repository.curation.CurationRepository;
import com.weady.weady.domain.curation.repository.curationCategory.CurationCategoryRepository;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.repository.DailySummaryRepository;
import com.weady.weady.common.error.errorCode.CurationErrorCode;
import com.weady.weady.common.error.errorCode.DailySummaryErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class CurationService {

    private final CurationCategoryRepository curationCategoryRepository; //location 별 curation 찾기 용도
    private final CurationRepository curationRepository; //curation 상세 조회 용도
    private final DailySummaryRepository dailySummaryRepository; //curation_category_id를 통한 큐레이션 찾기 용도

    /**
     * 지역별 날씨에 맞는 큐레이션 제공
     * @return curationByLocationResponseDto
     * @throws ...
     */
//    public CurationCategoryResponse.curationByLocationResponseDto getCurationByLocation(Long locationId){
//        CurationCategory curationCategory = curationCategoryRepository.findByLocationId(locationId)
//                .orElseThrow(() -> new EntityNotFoundException("CurationCategory not found")); //error status로 교체해야
//
//
//        //오늘 날씨 report 가져오는 로직 있어야...
//        //그 이후 필터링 진행
//    }


    /**
     * 큐레이션 상세정보 조회
     * @return curationByCurationIdResponseDto
     * @thorws CurationErrorCode.CURATION_NOT_FOUND 큐레이션이 존재하지 않을경우 예외 발생
     */
    public CurationByCurationIdResponseDto getSpecificCuration(Long curationId){
        Curation curation = curationRepository.findById(curationId)
                .orElseThrow(() -> new BusinessException(CurationErrorCode.CURATION_NOT_FOUND));

        String curationTitle = curation.getTitle();
        List<CurationImg> imgs = curation.getImgs();

        return CurationMapper.toCurationResponseDto(curationId, curationTitle, imgs);

    }

    /**
     * curation_category 넘겨주기
     * @return List<CurationCategoryResponse.curationCategoryResponseDto>
     * @throws ...
     */
    public List<CurationCategoryResponseDto> getCurationCategory(){
        List<CurationCategory> curationCategories = curationCategoryRepository.findAll();

        return CurationCategoryMapper.toDtoList(curationCategories);
    }


    /**
     * curation_category_id 로 위치별 큐레이션 조회하기
     * @return
     * @thorws DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND
     */
    public CurationByLocationResponseDto getCurationByCurationCategoryId(Long curationCategoryId){

        //curationCategoryId로 해당하는 curationCategory 가져오기
        CurationCategory curationCategory = curationCategoryRepository.findById(curationCategoryId)
                .orElseThrow(() -> new BusinessException(CurationErrorCode.CURATION_CATEGORY_NOT_FOUND));

        //curationCategoryId로 해당하는 location_id 찾기
        Long locationId = curationCategory.getLocation().getId();


        //location_id로 dailySummary 가져오기
        DailySummary dailySummary = dailySummaryRepository.findByLocationId(locationId)
                .orElseThrow(() -> new BusinessException(DailySummaryErrorCode.DAILY_SUMMARY_NOT_FOUND));


        //가져온 dailySummary에서 season, weather tag 가져오기
        SeasonTag seasonTag = dailySummary.getSeasonTag();
        WeatherTag weatherTag = dailySummary.getWeatherTag();


        String locationName = curationCategory.getViewName();

        //태그로 큐레이션 필터링
        List<Curation> curations = curationCategory.getCurations().stream()
                .filter(curation ->
                        curation.getSeasonTag().equals(seasonTag) &&
                        curation.getWeatherTag().equals(weatherTag)
                ).collect(Collectors.toList());

        return CurationCategoryMapper.toCurationByLocationResponseDto(locationId, locationName, curations);
    }



}
