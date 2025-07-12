package com.weady.weady.common.error.exception;

import com.weady.weady.common.error.errorCode.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 로직 상의 모든 예외를 포함하는 최상위 예외 클래스입니다.
 * 이후에 로직이 복잡해질 경우 각 도메인 별로 Exception을 쪼는 방향으로 리팩토링합니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        // 부모 클래스(RuntimeException)의 생성자를 호출하여 메시지를 설정
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}