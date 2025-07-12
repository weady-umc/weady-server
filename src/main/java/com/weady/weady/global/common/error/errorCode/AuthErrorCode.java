package com.weady.weady.global.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTHENTICATION_FAILED(401, "인증에 실패했습니다."),
    AUTHORIZATION_FAILED(403, "접근 권한이 없습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다.");

    private final int code;
    private final String message;
}