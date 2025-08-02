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
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "유저 온보딩(가입) API", description = "사용자의 이름, 성별, 스타일 카테고리, 약관 동의 여부를 등록합니다.")
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<OnboardResponse>> onboard(@RequestBody @Valid OnboardRequest request) {
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(userService.onboard(request)));
    }

    @Operation(summary = "유저 현재위치 업데이트 API", description = "사용자의 휴대폰 현재위치를 업데이트합니다.(홈화면에 들어갈 경우 호출해주시기 바랍니다.)")
    @PatchMapping("/now-location")
    public ResponseEntity<ApiResponse<UpdateNowLocationResponse>> updateNowLocation(@RequestBody UpdateNowLocationRequest request){
        return ResponseEntityUtil.buildDefaultResponseEntity(ApiSuccessResponse.of(userService.updateNowLocation(request)));
    }
    @Operation(summary = "유저 기본(현재)위치 조회 API", description = "사용자의 기본위치가 설정되어있으면 기본위치의 Id, 설정되어있지 않으면 현재위치의 Id를 반환" )
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
