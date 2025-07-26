package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherErrorCode implements ErrorCode {
    WEATHER_SNAPSHOT_NOT_FOUND(404, "날씨 snapshot 데이터를 찾을 수 없습니다.");

    private final int code;
    private final String message;
}
