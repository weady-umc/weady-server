package com.weady.weady.domain.curation.repository.curation;

import com.weady.weady.domain.curation.entity.Curation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCurationRepository extends JpaRepository<Curation,Long> {

}
