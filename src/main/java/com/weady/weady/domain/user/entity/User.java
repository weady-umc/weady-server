package com.weady.weady.domain.user.entity;

import com.weady.weady.domain.location.entity.Location;
import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
@Builder
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

    private String name;
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
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFavoriteLocation> userFavoriteLocations = new ArrayList<>();


    // 연관관계편의메소드
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
}
