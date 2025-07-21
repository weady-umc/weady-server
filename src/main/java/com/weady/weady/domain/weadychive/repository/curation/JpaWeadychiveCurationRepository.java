package com.weady.weady.domain.weadychive.repository.curation;

import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaWeadychiveCurationRepository extends JpaRepository<WeadychiveCuration,Long> {
    List<WeadychiveCuration> findAllByUserId(Long userId);
}
