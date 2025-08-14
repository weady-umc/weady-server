package com.weady.weady.domain.user.entity;

import lombok.Getter;

@Getter
public enum State {
    ACTIVE("활성화"),
    DELETED("삭제됨");

    private final String description;

    State(String description) {
        this.description = description;
    }
}
