package com.weady.weady.domain.user.controller;

import com.weady.weady.domain.user.dto.ExampleUserResponse;
import com.weady.weady.domain.user.service.ExampleUserService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
import com.weady.weady.global.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.global.util.ResponseEntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExampleUserController {
    private final ExampleUserService exampleUserService;
    @GetMapping("/api/v1/example/user")
    public ResponseEntity<ApiResponse<ExampleUserResponse.ExampleUserResponseDto>> getExampleUser(){
        ExampleUserResponse.ExampleUserResponseDto response = exampleUserService.getUser();

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(response));
    }
}
