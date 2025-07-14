package com.weady.weady.domain.location.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository{
    private final JpaLocationRepository jpaLocationRepository;
}
