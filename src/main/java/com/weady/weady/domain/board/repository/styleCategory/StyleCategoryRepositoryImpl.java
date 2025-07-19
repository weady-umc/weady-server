package com.weady.weady.domain.board.repository.styleCategory;

import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StyleCategoryRepositoryImpl implements StyleCategoryRepository{

    private final JpaStyleCategoryRepository jpaStyleCategoryRepository;

    @Override
    public Optional<ClothesStyleCategory> findById(Long id){
        return jpaStyleCategoryRepository.findById(id);
    }

    @Override
    public List<ClothesStyleCategory> findAllById(Iterable<Long> ids){
        return jpaStyleCategoryRepository.findAllById(ids);
    }
}
