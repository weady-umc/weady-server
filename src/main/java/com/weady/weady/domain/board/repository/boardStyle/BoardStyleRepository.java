package com.weady.weady.domain.board.repository.boardStyle;

import com.weady.weady.domain.board.entity.board.BoardStyle;

import java.util.List;
import java.util.Optional;

public interface BoardStyleRepository {
    List<BoardStyle> saveAll(List<BoardStyle> boardStyle);
    Optional<BoardStyle> findById(Long id);
}
