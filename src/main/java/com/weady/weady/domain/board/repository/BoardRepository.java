package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;

public interface BoardRepository {
    Board save(Board board);
}
