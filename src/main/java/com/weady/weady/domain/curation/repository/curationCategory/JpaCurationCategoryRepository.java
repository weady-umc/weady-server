package com.weady.weady.domain.curation.repository.curationCategory;

import com.weady.weady.domain.curation.entity.CurationCategory;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCurationCategoryRepository extends JpaRepository<CurationCategory,Long> {

    Optional<CurationCategory> findByLocationId(Long locationId);
}
