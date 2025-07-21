package com.weady.weady.domain.user.repository;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserFavoriteLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteLocationRepository extends JpaRepository<UserFavoriteLocation, Integer> {

    //지역 중복체크
    Boolean existsByUserAndLocation(User user, Location location);
}
