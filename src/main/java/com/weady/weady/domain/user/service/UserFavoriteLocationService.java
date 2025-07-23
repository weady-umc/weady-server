package com.weady.weady.domain.user.service;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.JpaLocationRepository;
import com.weady.weady.domain.location.repository.LocationRepository;
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
    private final LocationRepository locationRepository;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        //hCode로 Location 엔티티 조회
        Location location = locationRepository.findLocationByhCode(hCode)
                .orElseThrow(() -> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));


        //이미 즐겨찾기로 추가되어있는지 중복 체크
        if(userFavoriteLocationRepository.existsByUserAndLocation(user,location))
            throw new BusinessException(LocationErrorCode.FAVORITE_ALREADY_EXISTS);

        //즐겨찾기 Entity생성 후 저장
        UserFavoriteLocation favoriteLocation = user.addFavoriteLocation(location);

        return userFavoriteLocationMapper.toAddResponseDto(favoriteLocation);
    }
    /**
     * 사용자의 즐겨찾기 지역을 삭제하는 비즈니스 로직을 수행합니다.
     * 삭제 대상이 기본 위치(defaultLocation)인 경우, 기본 위치 설정을 먼저 해제합니다.
     *
     * @param favoriteId 삭제할 즐겨찾기 관계의 ID (user_favorite_location 테이블의 PK)
     * @throws BusinessException (AuthErrorCode.UNAUTHORIZED_USER) - 인증된 사용자를 찾을 수 없을 때
     * @throws BusinessException (UserErrorCode.FAVORITE_NOT_FOUND) - 해당 ID의 즐겨찾기가 존재하지 않을 때
     */
    @Transactional
    public void deleteUserFavoriteLocation(Long favoriteId){

        //userId를 가져와서 User 엔티티 조회
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        //favoriteId로 UserFavoriteLocation 엔티티 조회
        UserFavoriteLocation deleteFavoriteLocation = userFavoriteLocationRepository.findById(favoriteId)
                .orElseThrow(() -> new BusinessException(LocationErrorCode.FAVORITE_NOT_FOUND));

        //삭제하려는 지역이 DefaultLocation인 경우, DefaultLocation을 null로 설정
        if(user.getDefaultLocation() != null && user.getDefaultLocation().getId().equals(deleteFavoriteLocation.getId()))
            user.setDefaultLocation(null);

        //즐겨찾기 지역 삭제
        Location deleteLocation = deleteFavoriteLocation.getLocation();
        user.removeFavorite(deleteLocation);
    }
}
