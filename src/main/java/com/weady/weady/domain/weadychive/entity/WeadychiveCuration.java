package com.weady.weady.domain.weadychive.entity;

import com.weady.weady.domain.curation.entity.Curation;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
//@Table(
//        name = "weadychive_curation",
//        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "curation_id"})
//)
public class WeadychiveCuration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name="user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name="curation_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Curation curation;
}
