package com.weady.weady.domain.auth.controller;

import com.weady.weady.domain.auth.dto.AuthRequest;
import com.weady.weady.domain.auth.dto.AuthResponse;
import com.weady.weady.domain.auth.service.OAuthService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
import com.weady.weady.global.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.global.util.ResponseEntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final OAuthService oAuthService;

    @PostMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse.LoginResponseDto>> socialLogin( @PathVariable String provider,
                                                                                   @RequestBody AuthRequest.LoginRequestDto request) {
        AuthResponse.LoginResponseDto response = oAuthService.socialLogin(provider, request.authorizationToken());
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(response));
    }
}