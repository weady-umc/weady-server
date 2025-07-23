package com.weady.weady.domain.auth.handler;

import com.weady.weady.domain.auth.model.OAuthAttributes;
import com.weady.weady.domain.user.entity.Provider;

// 모든 소셜 로그인 핸들러의 규격(interface)을 정의
public interface SocialLoginHandler {

    // 이 핸들러가 어떤 소셜 로그인을 지원하는지 반환
    Provider getProviderType();

    // 인가 코드를 받아 최종 사용자 정보를 OAuthAttributes 형태로 반환
    OAuthAttributes getUserProfile(String authorizationCode);
}