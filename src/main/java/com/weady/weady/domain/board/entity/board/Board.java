package com.weady.weady.domain.board.entity.board;

import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.user.entity.User;
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
    private Boolean isPublic;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer imgCount;

    @Setter
    @Builder.Default
    private Integer goodCount = 0;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    // report
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    // 이미지
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardImg> boardImgList = new ArrayList<>();

    // 장소 정보
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardPlace> boardPlaceList = new ArrayList<>();

    // 스타일 태그
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardStyle> boardStyleList = new ArrayList<>();

    // 좋아요
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardGood> boardGoodList = new ArrayList<>();


    public void updateBoardPlaceList(List<BoardPlace> boardPlaceList){
        boardPlaceList.forEach(boardPlace -> {boardPlace.setBoard(this);});
        this.boardPlaceList = boardPlaceList;
    }

    public void updateBoardStyleList(List<BoardStyle> boardStyleList){
        boardStyleList.forEach(boardStyle -> {boardStyle.setBoard(this);});
        this.boardStyleList = boardStyleList;
    }
}
