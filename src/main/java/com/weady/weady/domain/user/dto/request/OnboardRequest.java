package com.weady.weady.domain.user.dto.request;

import com.weady.weady.common.validation.annotation.Unique;
import com.weady.weady.domain.user.entity.Gender;
import com.weady.weady.domain.user.entity.TermsType;
import com.weady.weady.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardRequest(
        @NotBlank(message = "닉네임은 필수 입력 값입니다.")
        @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하로 입력해주세요.")
        @Unique(entity = User.class, field = "name", message = "이미 사용 중인 닉네임입니다.")
        String name,
        @NotNull(message = "성별은 필수 입력 값입니다.")
        @Schema(
                description = "사용자 성별",
                allowableValues = {"W", "M", "NONE"},
                example = "W"
        )
        Gender gender,
        List<Long> styleIds,

        @NotEmpty(message = "약관 동의 정보는 필수입니다.")
        List<AgreementDto> agreements

){

        public record AgreementDto(
                @NotNull(message = "약관 타입은 필수입니다.")
                TermsType termsType,

                @NotNull(message = "동의 여부는 필수입니다.")
                Boolean isAgreed
        ) {}
}