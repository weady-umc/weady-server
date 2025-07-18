package com.weady.weady.domain.auth.dto;

public class AuthRequest {
    public record LoginRequestDto( String authorizationCode ) {}

    public record ReissueRequestDto( String refreshToken ) {}
}

