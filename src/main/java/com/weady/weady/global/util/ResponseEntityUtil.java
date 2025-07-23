package com.weady.weady.global.util;

import com.weady.weady.global.common.apiResponse.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityUtil {
    public static <T> ResponseEntity<ApiResponse<T>> buildResponseEntityWithStatus(ApiResponse<T> body, HttpStatus status) {
        return ResponseEntity.status(status).body(body);
    }

    public static <T>ResponseEntity<ApiResponse<T>> buildDefaultResponseEntity(ApiResponse<T> body) {
        return ResponseEntity.ok(body);
    }

    public static ResponseEntity<Void> buildSimpleResponseEntity() {
        return ResponseEntity.ok().build();
    }
}