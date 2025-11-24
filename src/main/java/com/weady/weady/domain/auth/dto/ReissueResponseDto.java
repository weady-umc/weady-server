package com.weady.weady.domain.auth.dto;

import lombok.Builder;

@Builder
public record ReissueResponseDto(String accessToken,
                                 String refreshToken) { }