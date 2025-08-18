package com.weady.weady.domain.user.controller;

import com.weady.weady.domain.user.dto.request.setDefaultLocationRequest;
import com.weady.weady.domain.user.dto.response.AddUserFavoriteLocationResponse;
import com.weady.weady.domain.user.dto.request.AddUserFavoriteLocationRequest;
import com.weady.weady.domain.user.dto.response.GetUserFavoriteLocationResponse;
import com.weady.weady.domain.user.dto.response.GetUserNowLocationResponse;
import com.weady.weady.domain.user.service.UserFavoriteLocationService;
import com.weady.weady.common.apiResponse.ApiResponse;
import com.weady.weady.common.apiResponse.ApiSuccessResponse;
import com.weady.weady.common.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/favorites")
public class UserFavoriteLocationController {

    private final UserFavoriteLocationService userFavoriteLocationService;

    @PostMapping
    @Operation(summary = "즐겨찾기 지역 추가 API")
    public ResponseEntity<ApiResponse<AddUserFavoriteLocationResponse>>
    addUserFavoriteLocation(@RequestBody AddUserFavoriteLocationRequest request){

        AddUserFavoriteLocationResponse response =
                userFavoriteLocationService.addUserFavoriteLocation(request.bCode());

        ApiResponse<AddUserFavoriteLocationResponse> responseWrapper =
                ApiSuccessResponse.of(response, "즐겨찾기 지역 추가에 성공했습니다.");

        return ResponseEntityUtil.buildResponseEntityWithStatus(responseWrapper, HttpStatus.CREATED);
    }

    @DeleteMapping("/{favoriteId}")
    @Operation(summary = "즐겨찾기 지역 삭제 API")
    public ResponseEntity<ApiResponse<Void>> deleteUserFavoriteLocation(@PathVariable Long favoriteId){

        userFavoriteLocationService.deleteUserFavoriteLocation(favoriteId);
        ApiResponse<Void> responseWrapper = ApiSuccessResponse.of("즐겨찾기 지역 삭제에 성공했습니다.");

        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }

    @GetMapping
    @Operation(summary = "즐겨찾기 지역 조회 API")
    public ResponseEntity<ApiResponse<List<GetUserFavoriteLocationResponse>>> getUserFavoriteLocations(){

        List<GetUserFavoriteLocationResponse> response = userFavoriteLocationService.getUserFavoriteLocations();
        ApiResponse<List<GetUserFavoriteLocationResponse>> responseWrapper =
                ApiSuccessResponse.of(response,"즐겨찾기 지역 조회에 성공했습니다.");
        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }

    @GetMapping("/nowLocations")
    @Operation(summary = "현재위치 지역 조회 api")
    public ResponseEntity<ApiResponse<GetUserNowLocationResponse>> getUserNowLocations(){

        GetUserNowLocationResponse response = userFavoriteLocationService.getUserNowLocations();
        ApiResponse<GetUserNowLocationResponse> responseWrapper =
                ApiSuccessResponse.of(response, "현재위치 지역 조회에 성공했습니다.");
        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }

    @PatchMapping("/default")
    @Operation(summary = "사용자 기본 위치 변경 API")
    public ResponseEntity<ApiResponse<Void>> setDefaultLocation(@RequestBody setDefaultLocationRequest request){

        userFavoriteLocationService.setDefaultLocation(request.userFavoriteLocationId());

        ApiResponse<Void> responseWrapper = ApiSuccessResponse.of("기본 위치 변경에 성공했습니다.");
        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }

    @DeleteMapping("/favorites/default")
    @Operation(summary = "사용자 기본 위치 설정 해제 API")
    public ResponseEntity<ApiResponse<Void>> unsetDefaultLocation() {

        userFavoriteLocationService.unsetDefaultLocation();

        ApiResponse<Void> responseWrapper = ApiSuccessResponse.of("기본 위치 설정이 해제되었습니다.");
        return ResponseEntityUtil.buildDefaultResponseEntity(responseWrapper);
    }
}
