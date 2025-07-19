package com.weady.weady.domain.board.repository.boardStyle;


import com.weady.weady.domain.board.entity.board.BoardStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardStyleRepositoryImpl implements BoardStyleRepository {

    private final JpaBoardStyleRepository jpaBoardStyleRepository;

    @Override
    public List<BoardStyle> saveAll(List<BoardStyle> boardStyles) {
        return jpaBoardStyleRepository.saveAll(boardStyles);
    }

    @Override
    public Optional<BoardStyle> findById(Long id) {
        return jpaBoardStyleRepository.findById(id);
    }
}
