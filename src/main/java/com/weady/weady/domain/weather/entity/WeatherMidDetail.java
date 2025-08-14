package com.weady.weady.domain.weather.entity;

import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherMidDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String midTermRegCode; // 중기예보 지역 코드

    private LocalDate date; // 언제 날씨 정보인지

    private Float pop; // 강수 확률

    @Enumerated(EnumType.STRING)
    private SkyCode amSkyCode; // 하늘 상태 코드 (1=맑음, 2=구름많음, 3=흐림, 4=비, 5=눈)
    @Enumerated(EnumType.STRING)
    private SkyCode pmSkyCode;

    private Float tmx; // 최고 기온
    private Float tmn; // 최저 기온

    public void apply(Float pop, SkyCode am, SkyCode pm, Float tmx, Float tmn) {
        this.pop = pop;
        this.amSkyCode = am;
        this.pmSkyCode = pm;
        this.tmx = tmx;
        this.tmn = tmn;
    }
}
