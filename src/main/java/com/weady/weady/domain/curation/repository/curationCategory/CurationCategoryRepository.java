package com.weady.weady.domain.curation.repository.curationCategory;

import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface CurationCategoryRepository {

    Optional<CurationCategory> findByLocationId(Long locationId);
    Optional<CurationCategory> findById(Long id);
    CurationCategory save(CurationCategory curationCategory);
    List<CurationCategory> findAll();
}
