package com.weady.weady.domain.board.entity;

import com.weady.weady.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class BoardPlace extends BaseEntity {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String placeName;
    private String placeAddress;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

}
