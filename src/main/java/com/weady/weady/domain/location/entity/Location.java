package com.weady.weady.domain.location.entity;

import com.weady.weady.domain.curation.entity.CurationCategory;
import com.weady.weady.common.entity.BaseEntity;
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
    private String bCode;

    @Column(length = 50)
    private String address1;
    @Column(length = 50)
    private String address2;
    @Column(length = 50)
    private String address3;
    @Column(length = 50)
    private String address4;

    private Integer nx;
    private Integer ny;

    @Column(length = 50)
    private String midTermRegCode;

}
