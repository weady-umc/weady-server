package com.weady.weady.domain.curation.entity;


import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Curation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "background_img_url")
    private String backgroundImgUrl;

    @ManyToOne
    @JoinColumn(name="curation_category_id",nullable = false)
    private CurationCategory curationCategory;

}
