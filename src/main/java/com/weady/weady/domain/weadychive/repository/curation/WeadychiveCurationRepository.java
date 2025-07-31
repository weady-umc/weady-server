package com.weady.weady.domain.weadychive.repository.curation;

import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WeadychiveCurationRepository extends JpaRepository<WeadychiveCuration,Long> {
    List<WeadychiveCuration> findAllByUserId(Long userId);


    //findAllByUserId를 Jpql과 fetchJoin을 이용해서 최적화
    @Query("SELECT wc FROM WeadychiveCuration wc JOIN FETCH wc.curation WHERE wc.user.id = :userId")
    List<WeadychiveCuration> findAllWithCurationByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndCurationId(Long userId, Long curationId);


}
