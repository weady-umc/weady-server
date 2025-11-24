package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.LocationWeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationWeatherSnapshotRepository extends JpaRepository<LocationWeatherSnapshot, Long> {

    Optional<LocationWeatherSnapshot> findByGridLocationIdAndDateAndTime(Long gridLocationId, Integer date, Integer time);

    List<LocationWeatherSnapshot> findByGridLocationIdAndDateOrderByTimeAsc(Long gridLocationId, Integer date);

    @Modifying
    @Query("DELETE FROM LocationWeatherSnapshot s WHERE s.date < :cutoff")
    int deleteOlderThan(@Param("cutoff") int cutoffDate);


    @Query("SELECT s FROM LocationWeatherSnapshot s " +
            "WHERE s.gridLocation.id IN :gridLocationIds AND s.date = :date")
    List<LocationWeatherSnapshot> findByGridLocationIdInAndDate(
            @Param("gridLocationIds") List<Long> gridLocationIds,
            @Param("date") int date
    );

    @Modifying
    @Query(value = "DELETE FROM location_weather_snapshot WHERE date < :cutoff LIMIT :limit",
            nativeQuery = true)
    int deleteOlderThanLimit(@Param("cutoff") int cutoff,
                             @Param("limit") int limit);
}