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


    @Query("""
    select uf, lwsd, ds
    from UserFavoriteLocation uf
    join uf.location l
    left join LocationWeatherShortDetail lwsd
           on lwsd.location = l
          and lwsd.time = :currentTime
          and lwsd.id = (
               select max(s.id)
               from LocationWeatherShortDetail s
               where s.location = l
                 and s.time = :currentTime
          )
    left join DailySummary ds
           on ds.location = l
          and ds.reportDate = :reportDate
          and ds.id = (
               select max(d2.id)
               from DailySummary d2
               where d2.location = l
                 and d2.reportDate = :reportDate
          )
    where uf.user.id = :userId
    """)
    List<Object[]> findFavoritesWithDetailsByUserId(
            @Param("userId") Long userId,
            @Param("reportDate") LocalDate reportDate,
            @Param("currentTime") int currentTime
    );

    @Query("""
      select distinct coalesce(dl.id, l2.id)
      from User u
      left join u.defaultLocation dl
      left join u.nowLocation l2
      where u.id = :userId
    """)
    Optional<Long> findDefaultOrNowLocationId(@Param("userId") Long userId);
}
