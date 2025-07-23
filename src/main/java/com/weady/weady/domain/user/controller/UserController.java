package com.weady.weady.domain.user.controller;

import com.weady.weady.domain.user.dto.UserRequest;
import com.weady.weady.domain.user.dto.UserResponse;
import com.weady.weady.domain.user.service.UserService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
import com.weady.weady.global.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.global.util.ResponseEntityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<UserResponse.onboardResponse>> onboard(@RequestBody @Valid UserRequest.onboardRequestDto request) {
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(userService.onboard(request)));
    }
}
