package com.weady.weady.domain.auth.mapper;

import com.weady.weady.domain.auth.dto.AuthResponse;
import com.weady.weady.domain.auth.entity.RefreshToken;
import com.weady.weady.domain.user.entity.User;

public class RefreshTokenMapper {
    public static RefreshToken buildRefreshToken(String token, User user) {
        return RefreshToken.builder()
                .token(token)
                .user(user)
                .build();
    }

    public static AuthResponse.ReissueResponseDto toReissueResponseDto(String accessToken, String refreshToken) {
        return AuthResponse.ReissueResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static AuthResponse.LoginResponseDto toLoginResponseDto(String accessToken, String refreshToken, boolean isNewUser) {
        return AuthResponse.LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .build();
    }
}
