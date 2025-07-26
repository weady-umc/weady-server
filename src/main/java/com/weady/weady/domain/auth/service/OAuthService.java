package com.weady.weady.domain.auth.service;

import com.weady.weady.domain.auth.dto.AuthRequest;
import com.weady.weady.domain.auth.dto.AuthResponse; // 수정된 DTO 임포트
import com.weady.weady.domain.auth.entity.RefreshToken;
import com.weady.weady.domain.auth.mapper.OAuthAttributeMapper;
import com.weady.weady.domain.auth.mapper.RefreshTokenMapper;
import com.weady.weady.domain.auth.model.OAuthAttributes;
import com.weady.weady.domain.auth.handler.SocialLoginHandler;
import com.weady.weady.domain.auth.repository.RefreshTokenRepository;
import com.weady.weady.domain.user.entity.Provider;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.common.error.errorCode.AuthErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.jwt.JwtTokenProvider;
import com.weady.weady.common.util.SecurityUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final RefreshTokenRepository refreshTokenRepository;


    private Map<Provider, SocialLoginHandler> handlerMap;

    @PostConstruct
    private void initializeHandlerMap() {
        handlerMap = loginHandlers.stream()
                .collect(Collectors.toMap(SocialLoginHandler::getProviderType, Function.identity()));
    }

    /** 소셜 로그인 처리 메서드
     * @param providerName: 소셜 로그인 제공자 이름 (예: "KAKAO", "NAVER", "GOOGLE")
     * @param authorizationCode: 소셜 로그인 인증 코드
     *
     * @return AuthResponse.LoginResponseDto: 로그인 응답 DTO
     * @throws AuthErrorCode.UNSUPPORTED_PROVIDER: 지원하지 않는 소셜 로그인 제공자일 경우 예외 발생
     * */
    @Transactional
    public AuthResponse.LoginResponseDto socialLogin(String providerName, String authorizationCode) {
        Provider provider = Provider.valueOf(providerName.toUpperCase());
        SocialLoginHandler handler = handlerMap.get(provider);

        if (handler == null) {
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_PROVIDER, providerName);
        }
        OAuthAttributes attributes = handler.getUserProfile(authorizationCode);

        SaveResult saveResult = saveOrUpdateUser(attributes);
        User user = saveResult.user();

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        saveOrUpdateRefreshToken(user, refreshToken);

        return RefreshTokenMapper.toLoginResponseDto(accessToken, refreshToken, saveResult.isNewUser());
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급하는 메서드
     * 이 메서드는 리프레시 토큰이 유효한지 검증하고, 해당 토큰에 연결된 사용자를 찾아 새로운 액세스 토큰과 리프레시 토큰을 생성합니다.
     * @param refreshTokenValue: 리프레시 토큰 값
     *
     * @return AuthResponse.ReissueResponseDto: 새로운 액세스 토큰과 리프레시 토큰을 포함한 응답 DTO
     * @throws AuthErrorCode.INVALID_REFRESH_TOKEN: 유효하지 않은 리프레시 토큰일 경우 예외 발생
     * */
    @Transactional
    public AuthResponse.ReissueResponseDto reissueTokens(AuthRequest.ReissueRequestDto requestDto) {
        String refreshTokenValue = requestDto.refreshToken();
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshTokenValue = jwtTokenProvider.createRefreshToken(user);

        refreshToken.updateToken(newRefreshTokenValue);
        return RefreshTokenMapper.toReissueResponseDto(newAccessToken, newRefreshTokenValue);
    }

    /**
     * 현재 로그인한 사용자를 로그아웃 시키는 메소드 (리프레시 토큰 삭제)
     */
    @Transactional
    public void logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        refreshTokenRepository.deleteByUserId(userId);
    }

    private SaveResult saveOrUpdateUser(OAuthAttributes attributes) {
        return userRepository.findByProviderAndSocialId(attributes.getProvider(), attributes.getSocialId())
                .map(user -> {
                    // 사용자가 이미 있을 경우, 이름이 비어있으면 OnBoarding 안한 것 이므로 새로운 유저로 간주
                    boolean isNewUser = user.getName().isEmpty();
                    return new SaveResult(user, isNewUser);
                })
                .orElseGet(() -> {
                    // 사용자가 없을 경우, 새로운 유저로 저장
                    User savedUser = userRepository.save(OAuthAttributeMapper.OAuthAttributesToUser(attributes));
                    return new SaveResult(savedUser, true);
                });
    }

    private void saveOrUpdateRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                existingToken -> existingToken.updateToken(refreshToken),
                () -> refreshTokenRepository.save(RefreshTokenMapper.buildRefreshToken(refreshToken, user))
        );
    }

    private record SaveResult(User user, Boolean isNewUser) {}
}