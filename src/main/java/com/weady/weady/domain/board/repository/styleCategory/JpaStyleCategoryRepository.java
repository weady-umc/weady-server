package com.weady.weady.domain.board.repository.styleCategory;

import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStyleCategoryRepository extends JpaRepository<ClothesStyleCategory, Long> {
}
