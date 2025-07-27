package com.weady.weady.global.common.error.errorCode;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurationErrorCode implements ErrorCode{

    CURATION_NOT_FOUND(404, "해당 큐레이션을 찾을 수 없습니다.");

    private final int code;
    private final String message;
}
