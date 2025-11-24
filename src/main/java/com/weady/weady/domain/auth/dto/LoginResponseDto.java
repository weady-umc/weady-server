package com.weady.weady.domain.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto( String accessToken,
                                String refreshToken,
                                boolean isNewUser) { }
