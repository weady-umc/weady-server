package com.weady.weady.domain.tags.repository.temperature;

import com.weady.weady.domain.tags.entity.TemperatureTag;

import java.util.Optional;

public interface TemperatureRepository {
    Optional<TemperatureTag> findById(Long id);
}

