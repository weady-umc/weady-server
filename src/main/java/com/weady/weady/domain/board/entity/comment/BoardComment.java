package com.weady.weady.domain.board.entity.comment;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class BoardComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BoardComment boardComment;


}
