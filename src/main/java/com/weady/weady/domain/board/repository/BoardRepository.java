package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface BoardRepository {
    Optional<Board> findById(Long id);
    Board save(Board board);
    Slice<Board> getFilteredAndSortedResults(Long weatherTagId, Long temperatureTagId, Long seasonTagId, Pageable pageable);

}


