package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationWeatherSnapshotRepository extends JpaRepository<LocationWeatherSnapshot, Long> {
    Optional<LocationWeatherSnapshot> findByLocationIdAndDateAndTime(Long locationId, Integer date, Integer time);

    List<LocationWeatherSnapshot> findByLocationIdAndDateOrderByTimeAsc(Long locationId, Integer date);

    @Modifying
    @Query("DELETE FROM LocationWeatherSnapshot s WHERE s.date < :cutoff")
    int deleteOlderThan(@Param("cutoff") int cutoffDate);

    @Query("SELECT s FROM LocationWeatherSnapshot s " +
            "WHERE s.location.id IN :locationIds AND s.date = :date")
    List<LocationWeatherSnapshot> findByLocationIdsAndDate(@Param("locationIds") List<Long> locationIds,
                                                           @Param("date") int date);


}
