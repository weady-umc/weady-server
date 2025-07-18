package com.weady.weady.global.common.apiResponse;

import lombok.Getter;

@Getter
public class ApiSuccessResponse<T> extends ApiResponse<T> {

    // 성공 시에는 code와 message가 고정되므로 생성자에서 값을 지정합니다.
    private static final int SUCCESS_CODE = 200;
    private static final String SUCCESS_MESSAGE = "Success";


    private ApiSuccessResponse(T data) {
        super(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    private ApiSuccessResponse(T data, String message) {
        super(SUCCESS_CODE, message, data);
    }

    // 정적 팩토리 메서드: 데이터만 받는 경우
    public static <T> ApiSuccessResponse<T> of(T data) {
        return new ApiSuccessResponse<>(data);
    }

    // 정적 팩토리 메서드: 데이터와 커스텀 메시지를 받는 경우
    public static <T> ApiSuccessResponse<T> of(T data, String message) {
        return new ApiSuccessResponse<>(data, message);
    }

    // 정적 팩토리 메서드: 메시지만 받는 경우
    public static ApiSuccessResponse<Void> of(String message) {
        return new ApiSuccessResponse<>(null, message);
    }
}