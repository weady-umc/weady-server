package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FashionErrorCode implements ErrorCode {
    FASHION_NOT_FOUND(404, "체감온도에 해당하는 옷차림 추천을 찾을 수 없습니다.");

    private final int code;
    private final String message;
}
