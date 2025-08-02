package com.weady.weady.domain.user.mapper;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import com.weady.weady.domain.tags.mapper.TagMapper;
import com.weady.weady.domain.user.dto.response.GetUserDefaultLocationResponse;
import com.weady.weady.domain.user.dto.response.OnboardResponse;
import com.weady.weady.domain.user.dto.response.UpdateNowLocationResponse;
import com.weady.weady.domain.user.dto.response.UpdateUserProfileResponse;
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

    public static UpdateNowLocationResponse toUpdateNowLocationResponse(Location location) {
        return UpdateNowLocationResponse.builder()
                .nowLocationId(location.getId())
                .address1(location.getAddress1())
                .address2(location.getAddress2())
                .address3(location.getAddress3())
                .address4(location.getAddress4())
                .build();
    }

    public static UpdateUserProfileResponse toUpdateUserProfileResponse(User user) {
        return UpdateUserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    public static GetUserDefaultLocationResponse toResponse(Location location){
        return GetUserDefaultLocationResponse.builder()
                .defaultLocationId(location.getId())
                .build();
    }
}
