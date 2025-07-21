package com.weady.weady.domain.tags.repository.season;

import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.tags.repository.weather.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SeasonRepositoryImpl implements SeasonRepository {
    private final JpaSeasonRepository jpaSeasonRepository;

    @Override
    public Optional<SeasonTag> findById(Long id){
        return jpaSeasonRepository.findById(id);
    }
}
