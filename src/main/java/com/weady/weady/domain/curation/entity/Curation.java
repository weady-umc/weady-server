package com.weady.weady.domain.curation.entity;


import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="curation_category_id",nullable = false)
    private CurationCategory curationCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="season_tag",nullable = false)
    private SeasonTag seasonTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="weather_tag",nullable = false)
    private WeatherTag weatherTag;

    @Builder.Default
    @OneToMany(mappedBy = "curation")
    @OrderBy("imgOrder ASC") //imgOrder 순서대로 저장
    private List<CurationImg> imgs = new ArrayList<>();


}
