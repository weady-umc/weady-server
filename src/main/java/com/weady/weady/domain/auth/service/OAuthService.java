package com.weady.weady.domain.auth.service;

import com.weady.weady.domain.auth.dto.AuthResponse; // 수정된 DTO 임포트
import com.weady.weady.domain.auth.mapper.OAuthAttributeMapper;
import com.weady.weady.domain.auth.model.OAuthAttributes;
import com.weady.weady.domain.auth.handler.SocialLoginHandler;
import com.weady.weady.domain.user.entity.Provider;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.global.jwt.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthService {
    private final List<SocialLoginHandler> loginHandlers;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private Map<Provider, SocialLoginHandler> handlerMap;

    @PostConstruct
    private void initializeHandlerMap() {
        handlerMap = loginHandlers.stream()
                .collect(Collectors.toMap(SocialLoginHandler::getProviderType, Function.identity()));
    }


    // 1. 반환 타입을 AuthResponse 로 변경
    public AuthResponse.LoginResponseDto socialLogin(String providerName, String authorizationCode) {
        Provider provider = Provider.valueOf(providerName.toUpperCase());
        SocialLoginHandler handler = handlerMap.get(provider);

        if (handler == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + providerName);
        }

        OAuthAttributes attributes = handler.getUserProfile(authorizationCode);

        // 2. saveOrUpdate 가 User 와 isNewUser 를 모두 담은 객체를 반환하도록 수정
        SaveResult saveResult = saveOrUpdate(attributes);
        User user = saveResult.user();

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // 3. AuthResponse 를 빌드할 때 isNewUser 값 추가
        return AuthResponse.LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(saveResult.isNewUser())
                .build();
    }

    // 4. 유저와 신규 여부를 함께 반환하기 위한 private record(또는 클래스) 선언
    private record SaveResult(User user, Boolean isNewUser) {}

    // 5. saveOrUpdate 메소드 로직 수정
    private SaveResult saveOrUpdate(OAuthAttributes attributes) {
        Optional<User> optionalUser = userRepository.findByProviderAndSocialId(
                attributes.getProvider(),
                attributes.getSocialId()
        );

        // 이미 가입된 유저인 경우
        if (optionalUser.isPresent()) {
            return new SaveResult(optionalUser.get(), false);
        }

        // 신규 유저인 경우
        User newUser = OAuthAttributeMapper.OAuthAttributesToUser(attributes);
        User savedUser = userRepository.save(newUser);
        // isNewUser: true 와 함께 새로 저장된 유저 정보 반환
        return new SaveResult(savedUser, true);
    }
}