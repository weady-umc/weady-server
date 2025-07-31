package com.weady.weady.domain.curation.entity;

import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CurationImg extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="img_url")
    private String imgUrl;

    @Column(name="img_order")
    private int imgOrder;

    @Column(name="img_address")
    private String imgAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="curation_id",nullable = false)
    private Curation curation;
}
