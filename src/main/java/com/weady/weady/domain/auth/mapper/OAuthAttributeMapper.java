package com.weady.weady.domain.auth.mapper;

import com.weady.weady.domain.auth.model.OAuthAttributes;
import com.weady.weady.domain.user.entity.State;
import com.weady.weady.domain.user.entity.User;

public class OAuthAttributeMapper {
    public static User OAuthAttributesToUser(OAuthAttributes oAuthAttributes){
        return User.builder()
                .email(oAuthAttributes.getEmail())
                .socialId(oAuthAttributes.getSocialId())
                .provider(oAuthAttributes.getProvider())
                .state(State.ACTIVE)
                .build();
    }
}
