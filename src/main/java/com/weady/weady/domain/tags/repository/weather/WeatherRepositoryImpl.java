package com.weady.weady.domain.tags.repository.weather;

import com.weady.weady.domain.tags.entity.WeatherTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WeatherRepositoryImpl implements WeatherRepository {

    private final JpaWeatherRepository jpaWeatherRepository;

    @Override
    public Optional<WeatherTag> findById(Long id) {
        return jpaWeatherRepository.findById(id);
    }
}
