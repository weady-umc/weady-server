package com.weady.weady.domain.board.repository.board;

import com.weady.weady.domain.board.entity.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBoardRepository extends JpaRepository<Board, Long> {

}
