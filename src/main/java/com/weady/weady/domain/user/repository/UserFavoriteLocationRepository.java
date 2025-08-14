package com.weady.weady.domain.user.repository;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserFavoriteLocationRepository extends JpaRepository<UserFavoriteLocation, Long> {

    //지역 중복체크
    Boolean existsByUserAndLocation(User user, Location location);

    List<UserFavoriteLocation> findByUser(User user);

    /**
     * --- [성능 최적화] ---
     * 특정 사용자의 모든 즐겨찾기 정보를 관련 날씨/요약 데이터와 함께 한 번의 쿼리로 조회합니다.
     * N+1 문제를 해결하기 위해 여러 엔티티를 LEFT JOIN으로 연결합니다.
     *
     * @param userId      조회할 사용자의 ID
     * @param reportDate  조회할 일일 요약 정보의 날짜 (오늘)
     * @param currentTime 조회할 현재 날씨 정보의 시간 (예: 1500)
     * @return Object 배열의 리스트. 각 배열은 [UserFavoriteLocation, LocationWeatherShortDetail, DailySummary] 순서로 구성됩니다.
     */
    @Query("SELECT uf, lwsd, ds " +
            "FROM UserFavoriteLocation uf " +
            "JOIN FETCH uf.location l " +
            "LEFT JOIN LocationWeatherShortDetail lwsd ON lwsd.location = l AND lwsd.time = :currentTime " +
            "LEFT JOIN DailySummary ds ON ds.location = l AND ds.reportDate = :reportDate " +
            "WHERE uf.user.id = :userId")
    List<Object[]> findFavoritesWithDetailsByUserId(
            @Param("userId") Long userId,
            @Param("reportDate") LocalDate reportDate,
            @Param("currentTime") int currentTime
    );

    @Query("""
      select coalesce(dl.location.id, nl.id)
      from User u
      left join u.defaultLocation dl
      left join dl.location
      left join u.nowLocation nl
      where u.id = :userId
    """)
    Optional<Long> findDefaultOrNowLocationId(Long userId);
}
