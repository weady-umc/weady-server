package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(404, "해당 사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(409, "이미 존재하는 사용자입니다."),
    INVALID_PASSWORD(400, "비밀번호가 일치하지 않습니다.");

    private final int code;
    private final String message;
}
