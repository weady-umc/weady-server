package com.weady.weady.domain.tags.repository.temperature;

import com.weady.weady.domain.tags.entity.TemperatureTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTemperatureRepository extends JpaRepository<TemperatureTag, Long> {
}
