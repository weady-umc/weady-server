package com.weady.weady.domain.curation.service;


import com.weady.weady.domain.curation.dto.Response.CurationByCurationIdResponseDto;
import com.weady.weady.domain.curation.dto.Response.CurationCategoryResponseDto;
import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.curation.entity.CurationImg;
import com.weady.weady.domain.curation.mapper.CurationCategoryMapper;
import com.weady.weady.domain.curation.mapper.CurationMapper;
import com.weady.weady.domain.curation.repository.curation.CurationRepository;
import com.weady.weady.domain.curation.repository.curationCategory.CurationCategoryRepository;
import com.weady.weady.global.common.error.errorCode.CurationErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class CurationService {

    private final CurationCategoryRepository curationCategoryRepository; //location 별 curation 찾기 용도
    private final CurationRepository curationRepository; //curation 상세 조회 용도

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

}
