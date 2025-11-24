package com.weady.weady.domain.user.entity;

import lombok.Getter;

@Getter
public enum Provider {
    KAKAO("kakao"),
    GOOGLE("google"),
    APPLE("apple"),
    ;

    private final String description;

    Provider(String description) {
        this.description = description;
    }
}
