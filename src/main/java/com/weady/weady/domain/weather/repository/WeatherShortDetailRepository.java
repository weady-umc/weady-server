package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


public interface WeatherShortDetailRepository extends JpaRepository<LocationWeatherShortDetail, Long> {

    @Query("SELECT l, lwsd, ds " +
            "FROM Location l " +
            "LEFT JOIN LocationWeatherShortDetail lwsd ON lwsd.location = l " +
            "LEFT JOIN DailySummary ds ON ds.location = l AND ds.reportDate = :todayDate " +
            "WHERE l.id = :locationId " +
            "ORDER BY lwsd.date ASC, lwsd.time ASC")
    List<Object[]> findShortWeatherInfoByLocationId(
            @Param("locationId") Long locationId,
            @Param("todayDate") LocalDate todayDate
    );
}
