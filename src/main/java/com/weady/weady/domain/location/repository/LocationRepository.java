package com.weady.weady.domain.location.repository;

import com.weady.weady.domain.location.entity.Location;

import java.util.Optional;

public interface LocationRepository {
    Optional<Location> findLocationByhCode(String hCode);
}
