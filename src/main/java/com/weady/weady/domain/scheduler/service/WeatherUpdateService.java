package com.weady.weady.domain.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weady.weady.common.constant.WeatherApiProperties;
import com.weady.weady.domain.location.repository.LocationRepository;
import com.weady.weady.domain.weather.repository.WeatherShortDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherUpdateService {

    private final LocationRepository locationRepository;
    private final WeatherShortDetailRepository weatherRepository;
    private final WeatherApiProperties apiProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void updateShortTermWeather() {}
}
