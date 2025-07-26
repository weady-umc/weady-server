package com.weady.weady.domain.tags.mapper;

import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import com.weady.weady.domain.tags.dto.SeasonTagResponseDto;
import com.weady.weady.domain.tags.dto.TemperatureTagResponseDto;
import com.weady.weady.domain.tags.dto.WeatherTagResponseDto;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;

public class TagMapper {
    public static ClothesStyleCategoryResponseDto toClothesStyleCategoryResponseDto(ClothesStyleCategory entity){
        return ClothesStyleCategoryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public static SeasonTagResponseDto toSeasonTagResponseDto(SeasonTag entity){
        return SeasonTagResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public static WeatherTagResponseDto toWeatherTagResponseDto(WeatherTag entity){
        return WeatherTagResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public static TemperatureTagResponseDto toTemperatureTagResponseDto(TemperatureTag entity){
        return TemperatureTagResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
