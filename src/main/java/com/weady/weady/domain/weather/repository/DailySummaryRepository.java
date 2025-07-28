package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailySummaryRepository extends JpaRepository<DailySummary,Long> {
    Optional<DailySummary> findByLocationId(Long locationId);
}
