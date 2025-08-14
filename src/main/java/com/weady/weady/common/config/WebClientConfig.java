package com.weady.weady.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Configuration
public class WebClientConfig {

    // 🔧 운영 기본은 false (필요할 때만 켜기)
    @Value("${weady.kma.logging.wiretap:false}") private boolean wiretapEnabled;
    @Value("${weady.kma.logging.basic:false}")   private boolean basicLogEnabled;

    @Bean(name = "kmaWebClient")
    public WebClient kmaWebClient() {
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10))
                        .addHandlerLast(new WriteTimeoutHandler(10)))
                .compress(true)   // 전송 압축
                .keepAlive(true); // 커넥션 재사용

        // 🔊 무거운 wiretap은 프로퍼티로만 ON
        if (wiretapEnabled) {
            httpClient = httpClient.wiretap("reactor.netty.http.client",
                    io.netty.handler.logging.LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        }

        WebClient.Builder b = WebClient.builder()
                .baseUrl("https://apis.data.go.kr")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; WeadyWeather/1.0)")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name())
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                                .build()
                );

        if (basicLogEnabled) {
            b = b.filter(logRequest()).filter(logResponse());
        }

        return b.build();
    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.debug("HTTP {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }
    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.debug("HTTP <- {}", res.statusCode());
            return Mono.just(res);
        });
    }
}
