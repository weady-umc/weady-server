package com.weady.weady.domain.fashion.repository;

import com.weady.weady.domain.fashion.entity.Fashion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FashionRepository extends JpaRepository<Fashion, Long> {
    @Query("SELECT f FROM Fashion f WHERE :temp BETWEEN f.startTemp AND f.endTemp")
    Optional<Fashion> findByTemperatureRange(@Param("temp") float temp);
}
