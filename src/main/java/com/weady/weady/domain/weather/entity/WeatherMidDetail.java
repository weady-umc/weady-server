package com.weady.weady.domain.weather.entity;

import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

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

    private Integer date; // 언제 날씨 정보인지

    private Integer pop; // 강수 확률

    private SkyCode skyCode; // 하늘 상태 코드 (1=맑음, 2=구름많음, 3=흐림, 4=비, 5=눈)

    private Integer tmx; // 최고 기온
    private Integer tmn; // 최저 기온
}
