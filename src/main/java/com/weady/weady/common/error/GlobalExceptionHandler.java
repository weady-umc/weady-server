package com.weady.weady.common.error;

import com.weady.weady.common.apiResponse.ApiErrorResponse;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.error.errorCode.CommonErrorCode;
import com.weady.weady.common.error.errorCode.ErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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

    /**
     * MethodArgumentNotValidException 타입의 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation Exception: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("유효성 검사에 실패했습니다.");

        ApiErrorResponse response = ApiErrorResponse.of(400, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}