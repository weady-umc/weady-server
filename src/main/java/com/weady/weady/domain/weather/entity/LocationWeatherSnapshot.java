package com.weady.weady.domain.weather.entity;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LocationWeatherSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    private Integer observationDate;

    private Integer observationTime;

    private Integer date; // 20250723

    private Integer time; // 1300

    private Float feelTmp; //체감온도

    private Float sky; //하늘

    private Float wsd; //풍속

    private Float pty; //강수형태 (눈,비, ...)

    private Float pop; //강수확률
}
