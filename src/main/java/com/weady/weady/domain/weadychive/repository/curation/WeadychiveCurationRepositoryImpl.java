package com.weady.weady.domain.weadychive.repository.curation;


import com.weady.weady.domain.weadychive.entity.WeadychiveCuration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WeadychiveCurationRepositoryImpl implements WeadychiveCurationRepository{

    private final JpaWeadychiveCurationRepository jpaWeadychiveCurationRepository;


    @Override
    public List<WeadychiveCuration> findAllByUserId(Long userId) {
        return jpaWeadychiveCurationRepository.findAllByUserId(userId);
    }

    @Override
    public WeadychiveCuration save(WeadychiveCuration weadychiveCuration) {
        return jpaWeadychiveCurationRepository.save(weadychiveCuration);
    }
}
