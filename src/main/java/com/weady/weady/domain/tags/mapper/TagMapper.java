package com.weady.weady.domain.tags.mapper;

import com.weady.weady.domain.tags.dto.TagResponse;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;

public class TagMapper {
    public static TagResponse.ClothesStyleCategoryResponseDto toClothesStyleCategoryResponseDto(ClothesStyleCategory entity){
        return TagResponse.ClothesStyleCategoryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
