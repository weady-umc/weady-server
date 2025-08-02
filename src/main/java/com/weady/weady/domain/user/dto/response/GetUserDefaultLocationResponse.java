package com.weady.weady.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "사용자의 기본위치가 설정되어있으면 기본위치의 Id, 설정되어있지 않으면 현재위치의 Id를 반환")
@Builder
public record GetUserDefaultLocationResponse(Long defaultLocationId) { }
