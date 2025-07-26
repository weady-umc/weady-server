package com.weady.weady.domain.weather.entity;

import lombok.Getter;

@Getter
public enum SkyCode {
    CLEAR("맑음"),
    PARTLY_CLOUDY("구름많음"),
    CLOUDY("흐림"),
    RAIN("비"),
    SNOW("눈"),
    ;
    private final String description;
    SkyCode(String description) {
        this.description = description;
    }
}
