package com.weady.weady.common.error.errorCode;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurationErrorCode implements ErrorCode{

    CURATION_NOT_FOUND(404, "해당 큐레이션을 찾을 수 없습니다."),
    CURATION_CATEGORY_NOT_FOUND(404,"해당 큐레이션 카테고리를 찾을 수 없습니다."),
    CURATION_ALREADY_SCRAPPED(409,"이미 스크랩한 큐레이션입니다.");

    private final int code;
    private final String message;
}
