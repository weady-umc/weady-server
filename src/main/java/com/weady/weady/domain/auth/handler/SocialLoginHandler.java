package com.weady.weady.domain.auth.handler;

import com.weady.weady.domain.auth.model.OAuthAttributes;
import com.weady.weady.domain.user.entity.Provider;

public interface SocialLoginHandler {

    Provider getProviderType();

    // accessToken 으로 사용자 정보 조회
    OAuthAttributes getUserProfileByAccessToken(String accessToken);
}