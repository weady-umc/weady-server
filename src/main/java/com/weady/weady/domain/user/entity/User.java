package com.weady.weady.domain.user.entity;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.domain.tags.entity.ClothesStyleCategory;
import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
@Builder
@Setter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "now_location_id")
    private Location nowLocation;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "default_location_id")
    private UserFavoriteLocation defaultLocation;

    @Builder.Default
    private String name = "";
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(columnDefinition = "TEXT")
    private String socialId;


    // Relation
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFavoriteLocation> userFavoriteLocations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserClothesStyleCategory> userClothesStyleCategories = new ArrayList<>();


    public void changeName(String name) {
        this.name = name;
    }
    public void changeGender(Gender gender) {
        this.gender = gender;
    }

    // FavoriteLocation 연관관계편의메소드
    public void addFavoriteLocation(Location location) {
        UserFavoriteLocation favorite = UserFavoriteLocation.builder()
                .user(this)
                .location(location)
                .build();
        this.userFavoriteLocations.add(favorite);
    }
    public void removeFavorite(Location location) {
        this.userFavoriteLocations.removeIf(favorite -> favorite.getLocation().equals(location));
    }

    // ClothesStyleCategory 연관관계 편의 메소드
    public void addStyleCategory(ClothesStyleCategory style) {
        if (style == null) return;
        boolean exists = userClothesStyleCategories.stream()
                .anyMatch(link -> link.getClothesStyleCategory().equals(style));
        if (exists) return;

        UserClothesStyleCategory link = UserClothesStyleCategory.builder()
                .user(this)
                .clothesStyleCategory(style)
                .build();

        userClothesStyleCategories.add(link);
    }

    public void removeStyleCategory(ClothesStyleCategory style) {
        if (style == null) return;
        userClothesStyleCategories.removeIf(
                link -> link.getClothesStyleCategory().equals(style)
        );
    }

    // 온보딩에서 전달된 스타일 리스트로 ‘완전 동기화’:
    // 1) 새 리스트에 없는 스타일은 모두 제거
    // 2) 새 리스트에 있는데 없던 스타일은 모두 추가
    public void syncStyleCategories(List<ClothesStyleCategory> newStyles) {
        Set<ClothesStyleCategory> newSet = new HashSet<>(newStyles);
        userClothesStyleCategories.removeIf(
                link -> !newSet.contains(link.getClothesStyleCategory())
        );
        newSet.forEach(this::addStyleCategory);
    }

    // UserClothesStyleCategory에서 ClothesStyleCategory를 가져오는 메소드
    public List<ClothesStyleCategory> getStyleCategories() {
        return userClothesStyleCategories.stream()
                .map(UserClothesStyleCategory::getClothesStyleCategory)
                .toList();
    }

}
