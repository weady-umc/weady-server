package com.weady.weady.common.error.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum S3ErrorCode implements ErrorCode {
    S3_UPLOAD_FAIL(500, "S3 업로드 실패"),
    S3_REMOVE_FAIL(500, "S3 삭제 실패"),
    ;

    private final int code;

    private final String message;

}
