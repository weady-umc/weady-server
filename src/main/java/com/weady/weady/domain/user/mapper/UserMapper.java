package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import com.weady.weady.domain.tags.mapper.TagMapper;
import com.weady.weady.domain.user.dto.response.OnboardResponse;
import com.weady.weady.domain.user.entity.User;

import java.util.List;

public class UserMapper {


    public static OnboardResponse toOnboardResponse(User user) {
        List<ClothesStyleCategoryResponseDto> clothesStyleCategoryResponses = user.getStyleCategories()
                .stream().map(TagMapper::toClothesStyleCategoryResponseDto).toList();

        return OnboardResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .categoryNames(clothesStyleCategoryResponses)
                .build();
    }

}
