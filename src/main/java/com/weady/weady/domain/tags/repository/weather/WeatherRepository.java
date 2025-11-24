package com.weady.weady.domain.tags.repository.weather;

import com.weady.weady.domain.tags.entity.WeatherTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<WeatherTag, Long> {
    Optional<WeatherTag> findByName(String name);
}
