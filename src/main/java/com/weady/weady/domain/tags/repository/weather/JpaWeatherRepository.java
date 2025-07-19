package com.weady.weady.domain.tags.repository.weather;

import com.weady.weady.domain.tags.entity.WeatherTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWeatherRepository extends JpaRepository<WeatherTag, Long> {
}
