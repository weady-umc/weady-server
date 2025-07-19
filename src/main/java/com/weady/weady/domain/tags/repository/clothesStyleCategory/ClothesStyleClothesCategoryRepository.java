package com.weady.weady.domain.tags.repository.clothesStyleCategory;

import com.weady.weady.domain.tags.entity.ClothesStyleCategory;

import java.util.List;
import java.util.Optional;

public interface ClothesStyleClothesCategoryRepository {
    Optional<ClothesStyleCategory> findById(Long id);
    List<ClothesStyleCategory> findAllById(Iterable<Long> ids);
}
