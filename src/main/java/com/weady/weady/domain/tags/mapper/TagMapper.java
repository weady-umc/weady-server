package com.weady.weady.domain.tags.mapper;

import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;

public class TagMapper {
    public static ClothesStyleCategoryResponseDto toClothesStyleCategoryResponseDto(ClothesStyleCategory entity){
        return ClothesStyleCategoryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
