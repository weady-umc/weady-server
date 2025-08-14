package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.LocationWeatherShortDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // 지난 레코드 삭제를 위한 쿼리
    @Modifying
    @Query("DELETE FROM LocationWeatherShortDetail lwsd " +
            "WHERE lwsd.observationDate < :date " +
            "OR (lwsd.observationDate = :date " +
            "AND lwsd.observationTime < :time)")
    void deleteOldRecords(@Param("date") int date, @Param("time") int time);

    // 효율적인 UPSERT 를 위해 특정 시간대의 기존 레코드를 미리 조회
    @Query("SELECT lwsd " +
            "FROM LocationWeatherShortDetail lwsd " +
            "WHERE lwsd.location.id IN :locationIds " +
            "AND lwsd.observationDate = :observationDate")
    List<LocationWeatherShortDetail> findExistingRecords(@Param("locationIds") List<Long> locationIds, @Param("observationDate") int observationDate);

    @Query("SELECT w FROM LocationWeatherShortDetail w " +
            "WHERE w.location.id IN :locationIds AND w.date = :date")
    List<LocationWeatherShortDetail> findByLocationIdsAndDate(@Param("locationIds") List<Long> locationIds,
                                                              @Param("date") int date);
}
