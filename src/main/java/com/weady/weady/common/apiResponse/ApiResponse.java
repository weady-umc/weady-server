package com.weady.weady.common.apiResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 모든 응답의 기본 구조를 정의하는 추상 클래스
// protected 생성자를 통해 new 키워드로 직접 인스턴스 생성을 막고, 정적 팩토리 메서드를 사용하도록 유도합니다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 성공 응답에서는 data 필드가 null일 수 없으므로, 실패 응답에서만 null이 될 수 있습니다.
// JsonInclude.Include.NON_NULL은 null인 필드를 JSON으로 변환할 때 제외시킵니다.
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ApiResponse<T> {

    protected int code;
    protected String message;
    protected T data;

    protected ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

}