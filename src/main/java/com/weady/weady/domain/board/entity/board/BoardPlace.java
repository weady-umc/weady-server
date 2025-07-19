package com.weady.weady.domain.board.entity.board;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @Setter
    private Board board;
}
