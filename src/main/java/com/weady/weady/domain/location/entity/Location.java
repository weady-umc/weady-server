package com.weady.weady.domain.location.entity;

import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Location extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String h_code;

    private Integer nx;
    private Integer ny;


    @OneToOne(mappedBy = "location")
    private CurationCategory curationCategory;

    // 여기 에서 user_favorite_location 과의 관계를 설정할 필요가 없음
}
