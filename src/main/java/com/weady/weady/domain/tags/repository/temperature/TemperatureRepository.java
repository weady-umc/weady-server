package com.weady.weady.domain.tags.repository.temperature;

import com.weady.weady.domain.tags.entity.TemperatureTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemperatureRepository extends JpaRepository<TemperatureTag, Long> {
}

