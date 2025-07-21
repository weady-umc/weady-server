package com.weady.weady.domain.curation.repository.curation;

import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;

import java.util.List;
import java.util.Optional;

public interface CurationRepository {
    Optional<Curation> findById(Long id);
    List<Curation> findAllById(Iterable<Long> ids);
    Curation save(Curation curation);
}
