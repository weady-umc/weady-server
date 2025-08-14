package com.weady.weady.domain.board.entity.board;

import com.weady.weady.domain.board.entity.comment.BoardComment;
import com.weady.weady.domain.tags.entity.SeasonTag;
import com.weady.weady.domain.tags.entity.TemperatureTag;
import com.weady.weady.domain.tags.entity.WeatherTag;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
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

    @Setter
    @Builder.Default
    private Integer commentCount = 0;


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

    // 옷 정보
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardBrand> boardBrandList = new ArrayList<>();

    //댓글
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardComment> boardCommentList = new ArrayList<>();


    // 좋아요
    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardGood> boardGoodList = new ArrayList<>();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public void updateBoard(Boolean isPublic, String content, SeasonTag seasonTag,
                            TemperatureTag temperatureTag, WeatherTag weatherTag) {
        this.isPublic = isPublic;
        this.content = content;
        this.seasonTag = seasonTag;
        this.temperatureTag = temperatureTag;
        this.weatherTag = weatherTag;
    }


    public void updateBoardPlaceList(List<BoardPlace> boardPlaceList){
        boardPlaceList.forEach(boardPlace -> {boardPlace.setBoard(this);});

        this.boardPlaceList.clear();
        this.boardPlaceList.addAll(boardPlaceList);
    }

    public void updateBoardStyleList(List<BoardStyle> boardStyleList){
        boardStyleList.forEach(boardStyle -> {boardStyle.setBoard(this);});

        // 게시글 수정 시 orphanRemoval 오류 발생 방지
        this.boardStyleList.clear();
        this.boardStyleList.addAll(boardStyleList);

    }

    public void updateBoardImgList(List<BoardImg> boardImgList){
        boardImgList.forEach(boardImg -> {boardImg.setBoard(this);});

        this.boardImgList.clear();
        this.boardImgList.addAll(boardImgList);
    }

    public void updateBoardBrandList(List<BoardBrand> boardBrandList){
        boardBrandList.forEach(boardBrand -> {boardBrand.setBoard(this);});

        this.boardBrandList.clear();
        this.boardBrandList.addAll(boardBrandList);
    }

    /// 좋아요 개수 증감 메서드 ///
    public void increaseGoodCount() {
        this.goodCount = this.goodCount + 1;
    }

    public void decreaseGoodCount() {
        this.goodCount = this.goodCount - 1;
    }

    ///  댓글 개수 증감 메서드 ///
    public void increaseCommentCount() {
        this.commentCount = this.commentCount + 1;
    }

    public void decreaseCommentCount(Integer children) {
        this.commentCount = this.commentCount - (1+children); // 부모 댓글 + 자식 댓글 개수 만큼 삭제
    }


}
