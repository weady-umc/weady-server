package com.weady.weady.domain.tags.repository.temperature;

import com.weady.weady.domain.tags.entity.TemperatureTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TemperatureRepositoryImpl implements TemperatureRepository {

    private final JpaTemperatureRepository jpaTemperatureRepository;

    @Override
    public Optional<TemperatureTag> findById(Long id) {
        return jpaTemperatureRepository.findById(id);
    }
}
