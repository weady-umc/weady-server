package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagsErrorCode implements ErrorCode {

    SEASON_TAG_NOT_FOUND(404, "해당 계절 태그를 찾을 수 없습니다."),
    TEMPERATURE_TAG_NOT_FOUND(404, "해당 온도 태그를 찾을 수 없습니다."),
    WEATHER_TAG_NOT_FOUND(404, "해당 날씨 태그를 찾을 수 없습니다.")
    ;

    private final int code;
    private final String message;
}
