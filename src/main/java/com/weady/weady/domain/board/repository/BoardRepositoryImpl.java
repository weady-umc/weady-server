package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final JpaBoardRepository jpaBoardRepository;

    @Override
    public Board save(Board board) {
        return jpaBoardRepository.save(board);
    }
}
