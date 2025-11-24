package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.location.entity.GridLocation;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface GridLocationRepository extends JpaRepository<GridLocation, Long> {
    Optional<GridLocation> findByNxAndNy(Integer nx, Integer ny);
}