package com.weady.weady.domain.curation.repository.curationCategory;

import com.weady.weady.domain.curation.entity.CurationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CurationCategoryRepositoryImpl implements CurationCategoryRepository{

    private final JpaCurationCategoryRepository jpaCurationCategoryRepository;


    @Override
    public Optional<CurationCategory> findByLocationId(Long locationId) {
        return jpaCurationCategoryRepository.findByLocationId(locationId);
    }

    @Override
    public Optional<CurationCategory> findById(Long id) {
        return jpaCurationCategoryRepository.findById(id);
    }

    @Override
    public CurationCategory save(CurationCategory curationCategory) {
        return jpaCurationCategoryRepository.save(curationCategory);
    }
}
