package com.weady.weady.domain.user.controller;

import com.weady.weady.domain.user.dto.request.OnboardRequest;
import com.weady.weady.domain.user.dto.request.UpdateNowLocationRequest;
import com.weady.weady.domain.user.dto.request.UpdateUserProfileRequest;
import com.weady.weady.domain.user.dto.response.*;
import com.weady.weady.domain.user.service.MyPageService;
import com.weady.weady.domain.user.service.UserService;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final MyPageService myPageService;

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<OnboardResponse>> onboard(@RequestBody @Valid OnboardRequest request) {
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(userService.onboard(request)));
    }

    @PatchMapping("/now-location")
    public ResponseEntity<ApiResponse<UpdateNowLocationResponse>> updateNowLocation(@RequestBody UpdateNowLocationRequest request){
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(userService.updateNowLocation(request)));
    }

    @GetMapping("/default-location")
    public ResponseEntity<ApiResponse<GetUserDefaultLocationResponse>> getUserDefaultLocation(){
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(userService.getUserDefaultLocation()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UpdateUserProfileResponse>> updateUserProfile(@RequestBody @Valid UpdateUserProfileRequest request) {
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(userService.updateUserProfile(request)));
    }

    @GetMapping("/my-page")
    public ResponseEntity<ApiResponse<GetMyPageResponse>> getMyPage(
            @RequestParam int year,
            @RequestParam int month) {
        GetMyPageResponse response = myPageService.getMyPage(year, month);

        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(response));
    }
}
