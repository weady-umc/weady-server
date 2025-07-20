package com.weady.weady.domain.tags.dto;

import lombok.Builder;

public class TagResponse {
    @Builder
    public record ClothesStyleCategoryResponseDto(Long id,
                                                  String name){}
}
