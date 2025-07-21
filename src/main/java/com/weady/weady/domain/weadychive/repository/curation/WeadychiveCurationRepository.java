package com.weady.weady.domain.weadychive.repository.curation;

import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;

import java.util.List;

public interface WeadychiveCurationRepository {
    List<WeadychiveCuration> findAllByUserId(Long userId);
    WeadychiveCuration save(WeadychiveCuration weadychiveCuration);
}
