package com.weady.weady.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "약관 타입 ->  AGE: 만 14세 이상 이용자 확인,  SERVICE: 서비스 이용 약관,  PRIVACY: 개인정보 처리 방침,  MARKETING: 마케팅 정보 수신 동의",
        example = "SERVICE")
public enum TermsType {
    AGE("만 14세 이상 이용자 확인"),
    SERVICE("서비스 이용 약관"),
    PRIVACY("개인정보 처리 방침"),
    MARKETING("마케팅 정보 수신 동의")
    ;

    private final String description;
    TermsType(String description) {
        this.description = description;
    }
}
