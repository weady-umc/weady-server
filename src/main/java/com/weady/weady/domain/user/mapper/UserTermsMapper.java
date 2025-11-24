package com.weady.weady.domain.user.mapper;


import com.weady.weady.domain.user.entity.TermsType;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.entity.UserTermsAgreement;

public class UserTermsMapper {
    public static UserTermsAgreement toUserTermsAgreement(User user, TermsType termsType, Boolean isAgreed) {
        return UserTermsAgreement.builder()
                    .user(user)
                    .termsType(termsType)
                    .isAgreed(isAgreed)
                    .build();
    }
}
