package com.weady.weady.domain.tags.dto;

import lombok.Builder;

@Builder
public record ClothesStyleCategoryResponseDto(Long id,
                                              String name){}
