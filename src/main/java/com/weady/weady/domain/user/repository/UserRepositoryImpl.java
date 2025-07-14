package com.weady.weady.domain.user.repository;

import com.weady.weady.domain.user.entity.Provider;
import com.weady.weady.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository{
    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByProviderAndSocialId(Provider provider, String socialId){
        return jpaUserRepository.findByProviderAndSocialId(provider, socialId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }
    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }
}
