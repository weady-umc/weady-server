package com.weady.weady.domain.board.entity;

import lombok.Getter;

@Getter
public enum Status {
    PENDING("처리 대기 중"),
    IN_PROGRESS("처리 중"),
    COMPLETED("처리 완료");

    private final String description;

    Status(String description) {
        this.description = description;
    }
}
