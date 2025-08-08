package com.weady.weady.domain.board.entity.board;


import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class BoardBrand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;

    private String product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @Setter
    private Board board;

}
