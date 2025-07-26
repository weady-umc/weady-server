package com.weady.weady.domain.weather.entity;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class DailySummary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private SeasonTag seasonTag;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private WeatherTag weatherTag;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private TemperatureTag temperatureTag;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    private LocalDate reportDate;

    private Float feelsLikeTmx;    //체감 최고온도
    private Float feelsLikeTmn;    //체감 최저온도
    private Float actualTmx;   //실제 최고온도
    private Float actualTmn;   //실제 최저온도
}
