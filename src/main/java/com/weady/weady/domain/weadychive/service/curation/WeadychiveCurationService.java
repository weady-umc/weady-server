package com.weady.weady.domain.weadychive.service.curation;


import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.repository.curation.CurationRepository;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.domain.weadychive.dto.curation.Request.ScrapCurationRequestDto;
import com.weady.weady.domain.weadychive.dto.curation.Response.CurationDto;
import com.weady.weady.domain.weadychive.dto.curation.Response.ScrappedCurationByUserResponseDto;
import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;
import com.weady.weady.domain.weadychive.mapper.curation.WeadychiveCurationMapper;

import com.weady.weady.domain.weadychive.repository.curation.WeadychiveCurationRepository;
import com.weady.weady.common.error.errorCode.CurationErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class WeadychiveCurationService {

    private final WeadychiveCurationRepository weadychiveCurationRepository;
    private final CurationRepository curationRepository;
    private final UserRepository userRepository;

    /**
     * 스크랩한 큐레이션 가져오기
     * @return scrappedCurationByUserResponseDto
     * @thorws UserErrorCode.USER_NOT_FOUND 사용자를 찾을 수 없을때 예외를 발생
     */
    public ScrappedCurationByUserResponseDto getScrappedCuration(){

        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        String userName = user.getName();


//        List<WeadychiveCuration> scrappedCurations = weadychiveCurationRepository.findAllByUserId(currentUserId);
//
//        List<Long> curationIds = scrappedCurations.stream() //리스트로 큐레이션 id들 가져오기
//                .map(scrap -> scrap.getCuration().getId())
//                .collect(Collectors.toList());
//
//        //여기서 SELECT * FROM curation WHERE id IN (1, 2, 1) 이 연산 실행. 이떄 1은 중복되지 않음
//        List<Curation> curations = curationRepository.findAllById(curationIds);

        List<WeadychiveCuration> scrappedCurations = weadychiveCurationRepository.findAllWithCurationByUserId(currentUserId);

        // curation 리스트 추출
        List<Curation> curations = scrappedCurations.stream()
                .map(WeadychiveCuration::getCuration)
                .collect(Collectors.toList());



        return WeadychiveCurationMapper.toScrappedCurationResponseDto(userName,curations);

    }

    /**
     * 큐레이션 스크랩하기
     * @return CurationDto
     * @thorws UserErrorCode.USER_NOT_FOUND 사용자를 찾을 수 없을때 예외를 발생
     * @thorws CurationErrorCode.CURATION_NOT_FOUND 큐레이션이 존재하지 않을경우 예외 발생
     */
    public CurationDto scrapCuration(ScrapCurationRequestDto requestDto){
        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Long curationId = requestDto.curationId();


        Curation curation = curationRepository.findById(curationId)
                .orElseThrow(()->new BusinessException(CurationErrorCode.CURATION_NOT_FOUND));

        WeadychiveCuration weadychiveCuration =  WeadychiveCurationMapper.toEntity(user , curation);

        weadychiveCurationRepository.save(weadychiveCuration);

        return WeadychiveCurationMapper.toCurationResponseDto(curationId, curation.getTitle(), curation.getBackgroundImgUrl());
    }

    /**
     * 큐레이션 스크랩 취소하기
     * @return
     * @throws ...
     */
    public void cancelCuration(Long curationId){
        Long currentUserId = SecurityUtil.getCurrentUserId();

        weadychiveCurationRepository.deleteByUserIdAndCurationId(currentUserId, curationId);
    }
}
