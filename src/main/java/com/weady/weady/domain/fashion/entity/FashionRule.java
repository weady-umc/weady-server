package com.weady.weady.domain.fashion.entity;

import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FashionRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    @OneToOne(fetch = FetchType.LAZY)
    private Fashion fashion;

    private Double startTemp;

    private Double endTemp;

    private Double startHumidity;

    private Double endHumidity;

    private Double startWind;

    private Double endWind;
}

