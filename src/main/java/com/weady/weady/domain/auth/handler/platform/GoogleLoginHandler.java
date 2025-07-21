package com.weady.weady.domain.auth.handler.platform;

import com.weady.weady.domain.auth.model.OAuthAttributes;
import com.weady.weady.domain.auth.handler.SocialLoginHandler;
import com.weady.weady.domain.user.entity.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleLoginHandler implements SocialLoginHandler {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private static final Provider PROVIDER_TYPE = Provider.GOOGLE;

    @Override
    public Provider getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public OAuthAttributes getUserProfile(String authorizationCode) {
        ClientRegistration provider = clientRegistrationRepository.findByRegistrationId(PROVIDER_TYPE.name().toLowerCase());
        String accessToken = getAccessToken(provider, authorizationCode);
        Map<String, Object> userAttributes = getUserAttributes(provider, accessToken);
        return parseUserAttributes(userAttributes);
    }

    private String getAccessToken(ClientRegistration provider, String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", provider.getClientId());
        params.add("client_secret", provider.getClientSecret());
        params.add("redirect_uri", provider.getRedirectUri());
        params.add("code", authorizationCode);

        log.info("Requesting Google Access Token with params: {}", params);

        try {
            Map<String, Object> response = WebClient.create()
                    .post().uri(provider.getProviderDetails().getTokenUri())
                    .bodyValue(params)
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).block();
            return (String) response.get("access_token");
        } catch (WebClientResponseException e) {
            // ★★★★★ 구글이 보내준 실제 에러 내용을 확인 ★★★★★
            log.error("Error from Google: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            // 예외를 다시 던져서 애플리케이션 흐름을 중단시킴
            throw e;
        }
    }

    private Map<String, Object> getUserAttributes(ClientRegistration provider, String accessToken) {
        return WebClient.create()
                .get().uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(header -> header.setBearerAuth(accessToken))
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).block();
    }

    private OAuthAttributes parseUserAttributes(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .email((String) attributes.get("email"))
                .socialId((String) attributes.get("sub"))
                .provider(PROVIDER_TYPE)
                .build();
    }
}