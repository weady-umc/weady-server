package com.weady.weady.domain.auth.handler.platform;

import com.nimbusds.jwt.JWTClaimsSet;
import com.weady.weady.common.security.verifier.AppleJwtVerifier;
import com.weady.weady.domain.auth.handler.SocialLoginHandler;
import com.weady.weady.domain.auth.model.OAuthAttributes;
import com.weady.weady.domain.user.entity.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleLoginHandler implements SocialLoginHandler {

    private static final Provider PROVIDER_TYPE = Provider.APPLE;
    private final AppleJwtVerifier verifier;

    @Override
    public Provider getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public OAuthAttributes getUserProfileByAccessToken(String accessToken) {
        // accessToken == iOS의 identityToken(JWT)
        JWTClaimsSet claims = verifier.verifyAndParse(accessToken);

        String socialId = claims.getSubject();
        String email = (String) claims.getClaim("email");

        return OAuthAttributes.builder()
                .email(email) // null 허용
                .socialId(socialId)
                .provider(PROVIDER_TYPE)
                .build();
    }
}
