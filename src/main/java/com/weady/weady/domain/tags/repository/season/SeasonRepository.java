package com.weady.weady.domain.tags.repository.season;

import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<SeasonTag, Long> {
}
