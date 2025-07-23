package com.weady.weady.domain.weadychive.repository.curation;

import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeadychiveCurationRepository extends JpaRepository<WeadychiveCuration, Long> {
    List<WeadychiveCuration> findAllByUserId(Long userId);
}
