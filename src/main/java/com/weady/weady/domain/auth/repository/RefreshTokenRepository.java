package com.weady.weady.domain.auth.repository;

import com.weady.weady.domain.auth.entity.RefreshToken;
import com.weady.weady.domain.user.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    void save(RefreshToken refreshToken);

    void deleteByUserId(Long userId);

}
