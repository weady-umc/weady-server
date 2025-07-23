package com.weady.weady.domain.auth.dto;

import lombok.Builder;

public class AuthResponse {
    @Builder
    public record LoginResponseDto( String accessToken,
                                    String refreshToken,
                                    boolean isNewUser) { //신규 가입자 여부

    }

    @Builder
    public record ReissueResponseDto(String accessToken,
                                     String refreshToken) {

    }
}
