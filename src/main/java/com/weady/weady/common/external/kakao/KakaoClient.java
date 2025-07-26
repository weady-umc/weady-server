package com.weady.weady.common.external.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoClient {

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
            .build();

    public KakaoApiResponse getRegionInfo(double longitude, double latitude) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/geo/coord2regioncode.json")
                        .queryParam("longitude", longitude)
                        .queryParam("latitude", latitude)
                        .build())
                .retrieve()
                .bodyToMono(KakaoApiResponse.class)
                .block();
    }
}
