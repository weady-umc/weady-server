package com.weady.weady.domain.user.entity;

import lombok.Getter;

@Getter
public enum Gender {
    W("여성"),
    M("남성"),
    NONE("해당없음");

    private final String description;

    Gender(String description) {
        this.description = description;
    }
}
