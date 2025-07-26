package com.weady.weady.domain.tags.controller;

import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import com.weady.weady.domain.tags.dto.SeasonTagResponseDto;
import com.weady.weady.domain.tags.dto.TemperatureTagResponseDto;
import com.weady.weady.domain.tags.dto.WeatherTagResponseDto;
import com.weady.weady.domain.tags.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagService tagService;

    @GetMapping("/clothes-style-categories")
    public List<ClothesStyleCategoryResponseDto> getClothesStyleCategories() {
        return tagService.getClothesStyleCategories();
    }

    @GetMapping("/temperature-tags")
    public List<TemperatureTagResponseDto> getTemperatureTags() {
        return tagService.getTemperatureTag();
    }

    @GetMapping("/weather-tags")
    public List<WeatherTagResponseDto> getWeatherTags() {
        return tagService.getWeatherTags();
    }

    @GetMapping("/season-tags")
    public List<SeasonTagResponseDto> getSeasonTags() {
        return tagService.getSeasonTags();
    }
}
