package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import com.weady.weady.domain.tags.mapper.TagMapper;
import com.weady.weady.domain.user.dto.response.ExampleUserResponse;
import com.weady.weady.domain.user.dto.response.UserResponse;
import com.weady.weady.domain.user.entity.User;

import java.util.List;

public class UserMapper {

    public static ExampleUserResponse.ExampleUserResponseDto toResponseDto(User user){
        return ExampleUserResponse.ExampleUserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserResponse.onboardResponse toOnboardResponse(User user) {
        List<ClothesStyleCategoryResponseDto> clothesStyleCategoryResponses = user.getStyleCategories()
                .stream().map(TagMapper::toClothesStyleCategoryResponseDto).toList();

        return UserResponse.onboardResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .categoryNames(clothesStyleCategoryResponses)
                .build();
    }

}
