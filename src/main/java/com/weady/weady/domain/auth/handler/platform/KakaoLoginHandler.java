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

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLoginHandler implements SocialLoginHandler {

    private final ClientRegistrationRepository clientRegistrationRepository;

    private static final Provider PROVIDER_TYPE = Provider.KAKAO;

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

        Map<String, Object> response = WebClient.create()
                .post().uri(provider.getProviderDetails().getTokenUri())
                .bodyValue(params)
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).block();
        return (String) Objects.requireNonNull(response).get("access_token");
    }

    private Map<String, Object> getUserAttributes(ClientRegistration provider, String accessToken) {
        return WebClient.create()
                .get().uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(header -> header.setBearerAuth(accessToken))
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).block();
    }

    private OAuthAttributes parseUserAttributes(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        return OAuthAttributes.builder()
                .email((String) kakaoAccount.get("email"))
                .socialId(String.valueOf(attributes.get("id")))
                .provider(PROVIDER_TYPE)
                .build();
    }
}
