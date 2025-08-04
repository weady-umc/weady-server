package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocationErrorCode implements ErrorCode {

    LOCATION_NOT_FOUND(404, "해당 지역을 찾을 수 없습니다."),
    FAVORITE_NOT_FOUND(404, "해당 즐겨찾기 지역을 찾을 수 없습니다."),
    NOW_LOCATION_NOT_FOUND(404, "현재 위치 지역을 찾을 수 없습니다."),
    FAVORITE_ALREADY_EXISTS(409, "이미 존재하는 즐겨찾기 지역입니다.");

    private final int code;
    private final String message;
}
