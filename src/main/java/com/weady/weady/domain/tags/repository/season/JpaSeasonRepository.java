package com.weady.weady.domain.tags.repository.season;

import com.weady.weady.domain.tags.entity.SeasonTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSeasonRepository extends JpaRepository<SeasonTag, Long> {
}
