package com.weady.weady.domain.curation.entity;


import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.global.common.entity.BaseEntity;
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

    @ManyToOne
    @JoinColumn(name="curation_category_id",nullable = false)
    private CurationCategory curationCategory;

    @ManyToOne
    @JoinColumn(name="season_tag",nullable = false)
    private SeasonTag seasonTag;

    @ManyToOne
    @JoinColumn(name="weather_tag",nullable = false)
    private WeatherTag weatherTag;

    @Builder.Default
    @OneToMany(mappedBy = "curation")
    private List<CurationImg> imgs = new ArrayList<>();


}
