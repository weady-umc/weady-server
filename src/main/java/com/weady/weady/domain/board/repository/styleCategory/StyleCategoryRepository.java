package com.weady.weady.domain.board.repository.styleCategory;

import com.weady.weady.domain.tags.entity.ClothesStyleCategory;

import java.util.List;
import java.util.Optional;

public interface StyleCategoryRepository {
    Optional<ClothesStyleCategory> findById(Long id);
    List<ClothesStyleCategory> findAllById(Iterable<Long> ids);
}
