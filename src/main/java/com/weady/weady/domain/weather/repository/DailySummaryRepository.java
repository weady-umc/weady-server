package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
    @Query("""
        select ds
        from DailySummary ds
        join fetch ds.seasonTag st
        join fetch ds.weatherTag wt
        join fetch ds.temperatureTag tt
        where ds.location.id = :locationId
          and ds.reportDate = :reportDate
    """)
    Optional<DailySummary> findByLocationIdAndReportDateWithTags(@Param("locationId") Long locationId,
                                                                 @Param("reportDate") LocalDate reportDate);
}
