package com.weady.weady.domain.curation.entity;


import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CurationCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="view_name")
    private String viewName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="location_id",nullable = false)
    private Location location;

    @Builder.Default
    @OneToMany(mappedBy = "curationCategory")
    private List<Curation> curations = new ArrayList<>();

}
