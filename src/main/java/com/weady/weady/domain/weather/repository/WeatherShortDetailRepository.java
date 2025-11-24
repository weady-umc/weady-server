package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherShortDetailRepository extends JpaRepository<LocationWeatherShortDetail, Long> {

    @Query("SELECT lwsd " +
            "FROM LocationWeatherShortDetail lwsd " +
            "WHERE lwsd.gridLocation.id = :gridLocationId " +
            "ORDER BY lwsd.date ASC, lwsd.time ASC")
    List<LocationWeatherShortDetail> findByGridLocationIdOrderByDateAscTimeAsc(
            @Param("gridLocationId") Long gridLocationId
    );

    @Modifying
    @Query("DELETE FROM LocationWeatherShortDetail lwsd " +
            "WHERE lwsd.observationDate < :date " +
            "OR (lwsd.observationDate = :date AND lwsd.observationTime < :time)")
    void deleteOldRecords(@Param("date") int date, @Param("time") int time);

    @Query("SELECT w FROM LocationWeatherShortDetail w " +
            "WHERE w.gridLocation.id = :gridLocationId AND w.observationDate = :observationDate")
    List<LocationWeatherShortDetail> findByGridLocationIdAndObservationDate(
            @Param("gridLocationId") Long gridLocationId,
            @Param("observationDate") int observationDate);

    @Query("SELECT d FROM LocationWeatherShortDetail d " +
            "WHERE d.gridLocation.id IN :gridLocationIds AND d.date = :date")
    List<LocationWeatherShortDetail> findByGridLocationIdInAndDate(
            @Param("gridLocationIds") List<Long> gridLocationIds,
            @Param("date") int date);
}