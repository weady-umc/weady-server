package com.weady.weady.domain.comment.entity;

import com.weady.weady.domain.board.entity.Board;
import com.weady.weady.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Comment {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn("user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn("board_id")
    private Board board;

    // 자기 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

}
