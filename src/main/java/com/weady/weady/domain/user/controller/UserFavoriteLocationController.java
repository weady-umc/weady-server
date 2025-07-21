package com.weady.weady.domain.user.controller;

import com.weady.weady.domain.user.dto.AddUserFavoriteLocationResponse;
import com.weady.weady.domain.user.dto.AdduserFavoriteLocationRequest;
import com.weady.weady.domain.user.service.UserFavoriteLocationService;
import com.weady.weady.global.common.apiResponse.ApiResponse;
import com.weady.weady.global.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.global.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/favorites")
public class UserFavoriteLocationController {

    private final UserFavoriteLocationService userFavoriteLocationService;

    @PostMapping
    @Operation(summary = "즐겨찾기 지역 추가 API")

    public ResponseEntity<ApiResponse<AddUserFavoriteLocationResponse>>
    addUserFavoriteLocation(@RequestBody AdduserFavoriteLocationRequest request){
        AddUserFavoriteLocationResponse response =
                userFavoriteLocationService.addUserFavoriteLocation(request.hCode());

        ApiResponse<AddUserFavoriteLocationResponse> responseWrapper =
                ApiSuccessResponse.of(response, "즐겨찾기 지역 추가에 성공했습니다.");
        return ResponseEntityUtil.buildResponseEntityWithStatus(responseWrapper, HttpStatus.CREATED);
    }
}
