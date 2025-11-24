package com.weady.weady.domain.auth.model;

import com.weady.weady.domain.user.entity.Provider;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class OAuthAttributes {
    private String email;
    private String socialId;
    private Provider provider;
}