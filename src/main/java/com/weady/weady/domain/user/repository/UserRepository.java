package com.weady.weady.domain.user.repository;

import com.weady.weady.domain.user.entity.Provider;
import com.weady.weady.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByProviderAndSocialId(Provider provider, String socialId);

    Optional<User> findByEmail(String email);

    User save(User user);
}
