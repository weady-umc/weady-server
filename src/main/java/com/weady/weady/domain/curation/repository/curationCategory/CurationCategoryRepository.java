package com.weady.weady.domain.curation.repository.curationCategory;

import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CurationCategoryRepository extends JpaRepository<CurationCategory, Long> {

    Optional<CurationCategory> findByLocationId(Long locationId);
    List<CurationCategory> findByLocationIdIn(List<Long> locationIds);
}
