package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KakaoErrorCode implements ErrorCode{

    KAKAO_RESPONSE_ERROR(500, "카카오 API 응답값에 오류가 발생했습니다."),
    ;

    private final int code;
    private final String message;
}
