package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LocationWeatherSnapshotRepository extends JpaRepository<LocationWeatherSnapshot, Long> {
    Optional<LocationWeatherSnapshot> findByLocationIdAndDateAndTime(Long locationId, Integer date, Integer time);
}
