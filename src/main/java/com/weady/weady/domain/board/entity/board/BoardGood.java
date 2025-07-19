package com.weady.weady.domain.board.entity.board;

import com.weady.weady.domain.user.entity.User;
import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardGood extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
