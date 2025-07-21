package com.weady.weady.domain.location.repository;

import com.weady.weady.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaLocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findLocationByH_code(String Hcode);
}
