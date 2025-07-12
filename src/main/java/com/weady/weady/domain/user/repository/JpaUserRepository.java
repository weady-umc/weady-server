package com.weady.weady.domain.user.repository;

import com.weady.weady.domain.user.entity.Provider;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndSocialId(Provider provider, String socialId);

    Optional<User> findByEmail(String email);
}
