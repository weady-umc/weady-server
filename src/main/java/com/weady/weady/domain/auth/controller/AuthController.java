package com.weady.weady.domain.auth.controller;

import com.weady.weady.domain.auth.dto.LoginRequestDto;
import com.weady.weady.domain.auth.dto.LoginResponseDto;
import com.weady.weady.domain.auth.dto.ReissueRequestDto;
import com.weady.weady.domain.auth.dto.ReissueResponseDto;
import com.weady.weady.domain.auth.service.OAuthService;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final OAuthService oAuthService;

    @PostMapping("/{provider}")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginWithAccessToken( @PathVariable String provider,
                                                                               @RequestBody LoginRequestDto request) {

        LoginResponseDto response = oAuthService.socialLoginWithAccessToken(provider, request);

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(response));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(){
        oAuthService.logout();
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of("로그아웃 성공"));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponseDto>> reissue(@RequestBody ReissueRequestDto request) {
        ReissueResponseDto response = oAuthService.reissueTokens(request);
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(response, "토큰 재발급 성공"));
    }

}