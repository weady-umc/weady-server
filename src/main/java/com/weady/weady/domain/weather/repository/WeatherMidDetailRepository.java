package com.weady.weady.domain.weather.repository;

import com.weady.weady.domain.weather.entity.WeatherMidDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface WeatherMidDetailRepository extends JpaRepository<WeatherMidDetail, Long> {
    List<WeatherMidDetail> findByMidTermRegCodeAndDateBetweenOrderByDateAsc(String midTermRegCode, LocalDate date, LocalDate date2);

    Optional<WeatherMidDetail> findByMidTermRegCodeAndDate(String midTermRegCode, LocalDate date);

}

