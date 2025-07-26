package com.weady.weady.domain.user.dto.response;

import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import lombok.Builder;

import java.util.List;

public class UserResponse {
    @Builder
    public record onboardResponse(Long userId,
                                  String name,
                                  List<ClothesStyleCategoryResponseDto> categoryNames) { }
}
