package com.weady.weady.domain.user.controller;

import com.weady.weady.domain.user.dto.UserRequest;
import com.weady.weady.domain.user.service.UserService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
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

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<Void>> onboard(@Valid @RequestBody UserRequest.OnboardRequestDto request){
        return null;
    }
}
