package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.user.dto.ExampleUserResponse;
import com.weady.weady.domain.user.entity.User;

public class UserMapper {

    public static ExampleUserResponse.ExampleUserResponseDto toResponseDto(User user){
        return ExampleUserResponse.ExampleUserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
