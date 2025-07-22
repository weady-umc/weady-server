package com.weady.weady.domain.location.repository;


import com.weady.weady.domain.location.entity.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {
    private final JpaLocationRepository jpaLocationRepository;

    @Override
    public Optional<Location> findLocationByhCode(String hCode) {
        return jpaLocationRepository.findLocationByhCode(hCode);
    }
}
