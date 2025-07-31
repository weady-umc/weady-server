package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.WeatherMidDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface WeatherMidDetailRepository extends JpaRepository<WeatherMidDetail, Long> {
    List<WeatherMidDetail> findByMidTermRegCodeAndDateBetweenOrderByDateAsc(String midTermRegCode, Integer startDate, Integer endDate);
}
