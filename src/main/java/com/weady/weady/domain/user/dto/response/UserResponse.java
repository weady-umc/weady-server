package com.weady.weady.domain.user.dto.response;

import com.weady.weady.domain.tags.dto.TagResponse;
import lombok.Builder;

import java.util.List;

public class UserResponse {
    @Builder
    public record onboardResponse(Long userId,
                                  String name,
                                  List<TagResponse.ClothesStyleCategoryResponseDto> categoryNames) { }
}
