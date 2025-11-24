package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KMAErrorCode implements ErrorCode {
    RATE_LIMIT_EXCEEDED(429, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    SERVICE_KEY_NOT_FOUND(404, "서비스 키를 찾을 수 없습니다."),
    KMA_XML_ERROR(500, "KMA XML 응답에 오류가 발생했습니다."),
    ;

    private final int code;
    private final String message;
}
