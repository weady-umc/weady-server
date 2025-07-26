package com.weady.weady.domain.weather.entity;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LocationWeatherShortDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    private Integer observationDate;

    private Integer observationTime;

    private Integer date;

    private Integer time;

    private Float tmp;

    private Float wsd;

    private SkyCode skyCode; // 1=맑음 2=구름많음 3=흐림 4=비 5=눈

    private Float pop;

    private Float pcp;

    private Float reh;
}
