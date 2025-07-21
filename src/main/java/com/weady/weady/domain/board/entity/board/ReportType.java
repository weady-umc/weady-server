package com.weady.weady.domain.board.entity.board;

import lombok.Getter;

@Getter
public enum ReportType {
    Spam ("스팸"),
    Harmful_Content ("유해 콘텐츠"),
    Disinformation ("잘못된 정보"),
    Hate_Activity ("혐오 활동"),
    Offensive_Content ("모욕적인 내용"),
    Illegal_Photo ("붋법 촬영물"),
    Other ("기타");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }
}