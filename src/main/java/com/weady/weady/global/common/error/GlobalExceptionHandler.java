package com.weady.weady.global.common.error;

import com.weady.weady.global.common.apiResponse.ApiErrorResponse;
import com.weady.weady.global.common.error.errorCode.CommonErrorCode;
import com.weady.weady.global.common.error.errorCode.ErrorCode;
import com.weady.weady.global.common.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 클래스입니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 타입의 예외를 처리합니다.
     * 비즈니스 로직 상에서 발생하는 모든 예측된 예외를 담당합니다.
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException: {}", ex.getMessage());
        ErrorCode errorCode = ex.getErrorCode();
        ApiErrorResponse response = ApiErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getCode()));
    }

    /**
     * 처리되지 않은 모든 예외를 처리합니다.
     * 예측하지 못한 서버 오류를 담당합니다.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        ApiErrorResponse response = ApiErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}