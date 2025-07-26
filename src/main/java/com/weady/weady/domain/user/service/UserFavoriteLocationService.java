package com.weady.weady.domain.user.service;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.user.dto.response.AddUserFavoriteLocationResponse;
import com.weady.weady.domain.user.dto.response.GetUserFavoriteLocationResponse;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import com.weady.weady.domain.user.mapper.UserFavoriteLocationMapper;
import com.weady.weady.domain.user.repository.UserFavoriteLocationRepository;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.domain.weather.entity.DailySummary;
import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import com.weady.weady.common.error.errorCode.LocationErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFavoriteLocationService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final UserFavoriteLocationRepository userFavoriteLocationRepository;

    /**
     * 사용자의 즐겨찾기 지역을 추가하는 비즈니스 로직을 수행.
     *
     * @param hCode 클라이언트로부터 받은 행정구역 코드
     * @return 생성된 즐겨찾기 정보가 담긴 DTO
     * @throws BusinessException (UserErrorCode.USER_NOT_FOUND) - 사용자를 찾을 수 없을 때
     * @throws BusinessException (LocationErrorCode.LOCATION_NOT_FOUND) - 지역을 찾을 수 없을 때
     * @throws BusinessException (LocationErrorCode.FAVORITE_ALREADY_EXISTS) - 사용자가 이미 해당 지역을 즐겨찾기에 추가했을 때
     */
    @Transactional
    public AddUserFavoriteLocationResponse addUserFavoriteLocation(String hCode) {

        //User 엔티티 조회
        User user = getAuthenticatedUser();

        //hCode로 Location 엔티티 조회
        Location location = locationRepository.findLocationBybCode(hCode)
                .orElseThrow(() -> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));


        //이미 즐겨찾기로 추가되어있는지 중복 체크
        if (userFavoriteLocationRepository.existsByUserAndLocation(user, location))
            throw new BusinessException(LocationErrorCode.FAVORITE_ALREADY_EXISTS);

        //즐겨찾기 Entity생성 후 저장
        UserFavoriteLocation favoriteLocation = user.addFavoriteLocation(location);

        return UserFavoriteLocationMapper.toAddResponseDto(favoriteLocation);
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
    public void deleteUserFavoriteLocation(Long favoriteId) {

        //User 엔티티 조회
        User user = getAuthenticatedUser();

        //favoriteId로 UserFavoriteLocation 엔티티 조회
        UserFavoriteLocation deleteFavoriteLocation = userFavoriteLocationRepository.findById(favoriteId)
                .orElseThrow(() -> new BusinessException(LocationErrorCode.FAVORITE_NOT_FOUND));

        //삭제하려는 지역이 DefaultLocation인 경우, DefaultLocation을 null로 설정
        if (user.getDefaultLocation() != null && user.getDefaultLocation().getId().equals(deleteFavoriteLocation.getId()))
            user.setDefaultLocation(null);

        //즐겨찾기 지역 삭제
        Location deleteLocation = deleteFavoriteLocation.getLocation();
        user.removeFavorite(deleteLocation);
    }

    @Transactional
    public List<GetUserFavoriteLocationResponse> getUserFavoriteLocations() {

        //User 엔티티 조회
        User user = getAuthenticatedUser();

        //쿼리에 필요한 오늘 날짜, 현재 시간을 조회
        LocalDate today = LocalDate.now();
        int currentTime = Integer.parseInt(LocalTime.now().format(DateTimeFormatter.ofPattern("HH00")));

        //JPQL 쿼리를 호출해서 데이터를 가져옴
        List<Object[]> results = userFavoriteLocationRepository.findFavoritesWithDetailsByUserId(user.getId(), today, currentTime);

        //Stream Api를 호출해서 ResponseDto 리스트로 변환
        return results.stream()
                .map(row -> {
                    UserFavoriteLocation favorite =  (UserFavoriteLocation) row[0];
                    LocationWeatherShortDetail weather = (LocationWeatherShortDetail) row[1];
                    DailySummary summary = (DailySummary) row[2];

                    return UserFavoriteLocationMapper.toGetResponse(favorite, weather, summary);
                })
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
