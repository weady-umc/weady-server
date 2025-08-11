package com.weady.weady.domain.weather.entity;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Setter
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

    private Float tmp; //기온

    private Float feelTmp; //체감온도

    private Integer sky; //하늘

    private Float wsd; //풍속

    private Integer pty; //강수형태 (눈,비, ...)

    private Float pop; //강수확률

    private Float pcp; //강수량
}
