package com.weady.weady.domain.auth.entity;

import com.weady.weady.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true, nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500)
    private String token; // 리프레시 토큰

    public void updateToken(String token) {
        this.token = token;
    }
}
