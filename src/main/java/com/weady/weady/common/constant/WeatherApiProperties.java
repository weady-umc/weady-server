package com.weady.weady.common.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kma.api")
public class WeatherApiProperties {
    private String shortTermKey;
    private String baseUrl;
}