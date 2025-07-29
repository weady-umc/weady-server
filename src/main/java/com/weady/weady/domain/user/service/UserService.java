package com.weady.weady.domain.user.service;

import com.weady.weady.common.error.errorCode.LocationErrorCode;
import com.weady.weady.common.external.kakao.KakaoRegionService;
import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.repository.clothesStyleCategory.ClothesStyleCategoryRepository;
import com.weady.weady.domain.user.dto.request.OnboardRequest;
import com.weady.weady.domain.user.dto.request.UpdateNowLocationRequest;
import com.weady.weady.domain.user.dto.request.UpdateUserProfileRequest;
import com.weady.weady.domain.user.dto.response.OnboardResponse;
import com.weady.weady.domain.user.dto.response.UpdateNowLocationResponse;
import com.weady.weady.domain.user.dto.response.UpdateUserProfileResponse;
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
    private final LocationRepository locationRepository;

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

    @Transactional
    public UpdateNowLocationResponse updateNowLocation(UpdateNowLocationRequest request){
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        String bCode = kakaoRegionService.getBCodeByCoordinates(request.longitude(), request.latitude());
        Location location = locationRepository.findLocationBybCode(bCode)
                .orElseThrow(() -> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));
        user.updateNowLocation(location);

        return UserMapper.toUpdateNowLocationResponse(location);

    }

    @Transactional
    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileRequest request){
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.changeName(request.name());
        user.changeProfileImageUrl(request.profileImageUrl());

        return UserMapper.toUpdateUserProfileResponse(user);
    }
}
