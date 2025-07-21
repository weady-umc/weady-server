package com.weady.weady.domain.tags.repository.weather;

import com.weady.weady.domain.tags.entity.WeatherTag;

import java.util.Optional;

public interface WeatherRepository {
    Optional<WeatherTag> findById(Long id);
}
