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

    private LocalDateTime observationTime;

    private Integer date;

    private Integer time;

    private Float tmp;

    private Float uuu;

    private Float vvv;

    private Integer vec;

    private Float wsd;

    private Float sky;

    private Float pty;

    private Float pop;

    private Float pcp;

    private Float reh;

    private Float wav;

    private Float sno;
}
