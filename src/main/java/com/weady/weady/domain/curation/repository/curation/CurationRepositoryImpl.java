package com.weady.weady.domain.curation.repository.curation;

import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.curation.entity.CurationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CurationRepositoryImpl implements CurationRepository{

    private final JpaCurationRepository jpaCurationRepository;


    @Override
    public Optional<Curation> findById(Long id) {
        return jpaCurationRepository.findById(id);
    }

    @Override
    public List<Curation> findAllById(Iterable<Long> ids) {
        return jpaCurationRepository.findAllById(ids);
    }

    @Override
    public Curation save(Curation curation) {
        return jpaCurationRepository.save(curation);
    }
}
