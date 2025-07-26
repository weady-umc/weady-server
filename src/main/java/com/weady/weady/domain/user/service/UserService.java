package com.weady.weady.domain.user.service;

import com.weady.weady.common.external.kakao.KakaoRegionService;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.repository.clothesStyleCategory.ClothesStyleCategoryRepository;
import com.weady.weady.domain.user.dto.request.OnboardRequest;
import com.weady.weady.domain.user.dto.response.OnboardResponse;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.mapper.UserMapper;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final KakaoRegionService kakaoRegionService;
    private final UserRepository userRepository;
    private final ClothesStyleCategoryRepository clothesStyleCategoryRepository;

    @Transactional
    public OnboardResponse onboard(OnboardRequest request){
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        List<ClothesStyleCategory> styleCategories = clothesStyleCategoryRepository.findAllById(request.styleIds());
        user.changeName(request.name());
        user.changeGender(request.gender());
        user.syncStyleCategories(styleCategories);

        return UserMapper.toOnboardResponse(user);
    }


}
