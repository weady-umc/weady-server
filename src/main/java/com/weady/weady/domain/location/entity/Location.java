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
    private String hCode;

    private Integer nx;
    private Integer ny;

    @Column(length = 20)
    private String midTermLandRegId;

    @Column(length = 20)
    private String midTermTempRegId;


    @OneToOne(mappedBy = "location")
    private CurationCategory curationCategory;

}
