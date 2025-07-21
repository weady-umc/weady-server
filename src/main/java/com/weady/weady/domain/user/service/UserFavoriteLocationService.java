package com.weady.weady.domain.user.service;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.JpaLocationRepository;
import com.weady.weady.domain.user.dto.AddUserFavoriteLocationResponse;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import com.weady.weady.domain.user.mapper.UserFavoriteLocationMapper;
import com.weady.weady.domain.user.repository.UserFavoriteLocationRepository;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.global.common.error.errorCode.LocationErrorCode;
import com.weady.weady.global.common.error.errorCode.UserErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import com.weady.weady.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFavoriteLocationService {

    private final UserRepository userRepository;
    private final JpaLocationRepository jpaLocationRepository;
    private final UserFavoriteLocationRepository userFavoriteLocationRepository;
    private final UserFavoriteLocationMapper userFavoriteLocationMapper;

    /**
     * 사용자의 즐겨찾기 지역을 추가하는 비즈니스 로직을 수행.
     * @param hCode 클라이언트로부터 받은 행정구역 코드
     * @return 생성된 즐겨찾기 정보가 담긴 DTO
     * @throws BusinessException (UserErrorCode.USER_NOT_FOUND) - 사용자를 찾을 수 없을 때
     * @throws BusinessException (LocationErrorCode.LOCATION_NOT_FOUND) - 지역을 찾을 수 없을 때
     * @throws BusinessException (LocationErrorCode.FAVORITE_ALREADY_EXISTS) - 사용자가 이미 해당 지역을 즐겨찾기에 추가했을 때
     */
    @Transactional
    public AddUserFavoriteLocationResponse addUserFavoriteLocation(String hCode){

        //userId를 가져와서 User 엔티티 조회
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        //hCode로 Location 엔티티 조회
        Location location = jpaLocationRepository.findLocationByH_code(hCode).orElseThrow(() -> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));

        //이미 즐겨찾기로 추가되어있는지 중복 체크하기
        if(userFavoriteLocationRepository.existsByUserAndLocation(user,location))
            throw new BusinessException(LocationErrorCode.FAVORITE_ALREADY_EXISTS);

        //즐겨찾기 Entity생성 후 저장하기
        UserFavoriteLocation favoriteLocation = user.addFavoriteLocation(location);

        return userFavoriteLocationMapper.toAddResponseDto(favoriteLocation);
    }
}
