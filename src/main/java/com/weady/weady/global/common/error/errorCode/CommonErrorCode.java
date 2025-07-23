package com.weady.weady.global.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(400, "입력 값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(404, "리소스를 찾을 수 없습니다.");

    private final int code;
    private final String message;
}
