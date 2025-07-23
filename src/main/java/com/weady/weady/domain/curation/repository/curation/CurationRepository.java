package com.weady.weady.domain.curation.repository.curation;

import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurationRepository extends JpaRepository<Curation, Long> {
}
