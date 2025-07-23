package com.weady.weady.domain.curation.repository.curationCategory;

import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurationCategoryRepository extends JpaRepository<CurationCategory, Long> {
}
