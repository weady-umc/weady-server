package com.weady.weady.domain.board.repository.boardPlace;

import com.weady.weady.domain.board.entity.board.BoardPlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBoardPlaceRepository extends JpaRepository<BoardPlace, Long> {
}
