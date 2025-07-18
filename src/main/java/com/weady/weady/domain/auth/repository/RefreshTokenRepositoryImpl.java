package com.weady.weady.domain.auth.repository;

import com.weady.weady.domain.auth.entity.RefreshToken;
import com.weady.weady.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository{
    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Override
    public Optional<RefreshToken> findByUser(User user){
        return jpaRefreshTokenRepository.findByUserId(user.getId());
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token);
    }

    @Override
    public void save(RefreshToken refreshToken) {
        jpaRefreshTokenRepository.save(refreshToken);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRefreshTokenRepository.findByUserId(userId)
                .ifPresent(jpaRefreshTokenRepository::delete);
    }
}
