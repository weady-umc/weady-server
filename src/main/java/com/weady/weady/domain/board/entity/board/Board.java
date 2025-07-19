package com.weady.weady.domain.board.entity.board;

import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer imgCount;

    // Relation
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardImg> boardImg = new ArrayList<>();

    // 장소 정보
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardPlace> boardPlaceList = new ArrayList<>();

    //날씨 태그
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_tag_id")
    private SeasonTag seasonTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temperature_tag_id")
    private TemperatureTag temperatureTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_tag_id")
    private WeatherTag weatherTag;

    // 스타일 태그
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BoardStyle> boardStyles = new ArrayList<>();

    // report
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Report> reports = new ArrayList<>();

}
