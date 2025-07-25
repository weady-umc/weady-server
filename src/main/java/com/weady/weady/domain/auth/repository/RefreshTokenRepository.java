package com.weady.weady.domain.auth.repository;

import com.weady.weady.domain.auth.entity.RefreshToken;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);

}
