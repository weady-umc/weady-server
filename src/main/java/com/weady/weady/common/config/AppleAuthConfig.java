package com.weady.weady.common.config;

import com.weady.weady.common.security.verifier.AppleJwtVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppleAuthConfig {

    @Bean
    public AppleJwtVerifier appleJwtVerifier(
            @Value("${spring.security.oauth2.client.provider.apple.issuer-uri}") String issuer,
            @Value("${spring.security.oauth2.client.registration.apple.client-id}") String audience,
            @Value("${spring.security.oauth2.client.provider.apple.jwk-set-uri}") String jwkSetUri
    ) {
        return new AppleJwtVerifier(issuer, audience, jwkSetUri, 60L); // 스큐 60초
    }
}

