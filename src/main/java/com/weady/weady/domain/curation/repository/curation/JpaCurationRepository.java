package com.weady.weady.domain.curation.repository.curation;

import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCurationRepository extends JpaRepository<Curation,Long> {
    List<Curation> findAllById(Iterable<Long> ids);
}
