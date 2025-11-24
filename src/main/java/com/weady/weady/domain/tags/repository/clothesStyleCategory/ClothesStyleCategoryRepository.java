package com.weady.weady.domain.tags.repository.clothesStyleCategory;

import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClothesStyleCategoryRepository extends JpaRepository<ClothesStyleCategory, Long> {

}
