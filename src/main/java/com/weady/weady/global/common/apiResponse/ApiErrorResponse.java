package com.weady.weady.global.common.apiResponse;

import com.weady.weady.global.common.error.errorCode.ErrorCode;
import lombok.Getter;

@Getter
public class ApiErrorResponse extends ApiResponse<Void> {

    // 실패 시에는 생성자를 통해 code와 message를 직접 입력받습니다.
    private ApiErrorResponse(int code, String message) {
        super(code, message, null);
    }

    // ErrorCode Enum을 사용하는 경우 (가장 권장)
    public static ApiErrorResponse of(ErrorCode errorCode) {
        return new ApiErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    // 코드를 직접 지정하는 경우
    public static ApiErrorResponse of(int code, String message) {
        return new ApiErrorResponse(code, message);
    }
}