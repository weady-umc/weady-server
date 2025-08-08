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

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleLoginHandler implements SocialLoginHandler {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private static final Provider PROVIDER_TYPE = Provider.GOOGLE;

    @Override
    public Provider getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public OAuthAttributes getUserProfileByAccessToken(String accessToken) {
        ClientRegistration provider = clientRegistrationRepository.findByRegistrationId(PROVIDER_TYPE.name().toLowerCase());

        Map<String, Object> attributes = WebClient.create()
                .get().uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(header -> header.setBearerAuth(accessToken))
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}).block();

        return OAuthAttributes.builder()
                .email((String) attributes.get("email"))
                .socialId((String) attributes.get("sub"))
                .provider(PROVIDER_TYPE)
                .build();
    }
}