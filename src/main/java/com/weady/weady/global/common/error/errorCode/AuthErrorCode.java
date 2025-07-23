package com.weady.weady.global.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTHENTICATION_FAILED(401, "인증에 실패했습니다."),
    AUTHORIZATION_FAILED(403, "접근 권한이 없습니다."),
    INVALID_ACCESS_TOKEN(401, "유효하지 않은 Access 토큰입니다."),
    INVALID_REFRESH_TOKEN(400, "유효하지 않은 Refresh 토큰입니다."),
    UNSUPPORTED_PROVIDER(400, "지원하지 않는 소셜 로그인입니다."),
    UNAUTHORIZED_USER(401, "인증되지 않은 사용자입니다."),
    ;


    private final int code;
    private final String message;
}