package com.weady.weady.common.external.kakao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class KakaoClient {

    private final WebClient webClient;

    public KakaoClient(@Value("${kakao.rest-api-key}") String kakaoApiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
                .build();

        log.info("🔑 Kakao API Key loaded: {}", kakaoApiKey);
    }

    public KakaoApiResponse getRegionInfo(double longitude, double latitude) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/geo/coord2regioncode.json")
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .build())
                .retrieve()
                .bodyToMono(KakaoApiResponse.class)
                .block();
    }
}
