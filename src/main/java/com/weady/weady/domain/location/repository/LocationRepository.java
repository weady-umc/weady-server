package com.weady.weady.domain.location.repository;

import com.weady.weady.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findLocationBybCode(String bCode);


    @Query("SELECT l.bCode FROM Location l WHERE l.id = :id")
    Optional<String> findBCodeById(@Param("id") Long id);


    @Query("SELECT l.id FROM Location l WHERE l.bCode LIKE CONCAT(:prefix, '%') AND FUNCTION('RIGHT', l.bCode, 5) = '00000'")
    List<Long> findIdsByBcodePrefix(@Param("prefix") String prefix);


    @Query("SELECT l.id FROM Location l WHERE l.bCode = :bCode")
    Long findIdByBCode(@Param("bCode") String bCode);

    @Query("select distinct l.midTermRegCode from Location l where l.midTermRegCode is not null")
    List<String> findDistinctMidTermRegCodes();


}
