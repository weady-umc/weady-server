package com.weady.weady.domain.tags.service;

import com.weady.weady.domain.tags.dto.ClothesStyleCategoryResponseDto;
import com.weady.weady.domain.tags.dto.SeasonTagResponseDto;
import com.weady.weady.domain.tags.dto.TemperatureTagResponseDto;
import com.weady.weady.domain.tags.dto.WeatherTagResponseDto;
import com.weady.weady.domain.tags.mapper.TagMapper;
import com.weady.weady.domain.tags.repository.clothesStyleCategory.ClothesStyleCategoryRepository;
import com.weady.weady.domain.tags.repository.season.SeasonRepository;
import com.weady.weady.domain.tags.repository.temperature.TemperatureRepository;
import com.weady.weady.domain.tags.repository.weather.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final ClothesStyleCategoryRepository clothesStyleCategoryRepository;
    private final SeasonRepository seasonRepository;
    private final TemperatureRepository temperatureRepository;
    private final WeatherRepository weatherRepository;

    public List<SeasonTagResponseDto> getSeasonTags(){
        return seasonRepository.findAll().stream()
                .map(TagMapper::toSeasonTagResponseDto)
                .toList();
    }

    public List<TemperatureTagResponseDto> getTemperatureTag(){
        return temperatureRepository.findAll().stream()
                .map(TagMapper::toTemperatureTagResponseDto)
                .toList();
    }

    public List<WeatherTagResponseDto> getWeatherTags() {
        return weatherRepository.findAll().stream()
                .map(TagMapper::toWeatherTagResponseDto)
                .toList();
    }

    public List<ClothesStyleCategoryResponseDto> getClothesStyleCategories() {
        return clothesStyleCategoryRepository.findAll().stream()
                .map(TagMapper::toClothesStyleCategoryResponseDto)
                .toList();
    }
}
