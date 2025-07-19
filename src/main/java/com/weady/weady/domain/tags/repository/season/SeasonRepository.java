package com.weady.weady.domain.tags.repository.season;

import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.WeatherTag;

import java.util.Optional;

public interface SeasonRepository {
    Optional<SeasonTag> findById(Long id);
}
