package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final JpaBoardRepository jpaBoardRepository;

    @Override
    public Optional<Board> findById(Long id) {return jpaBoardRepository.findById(id);}

    @Override
    public Board save(Board board) {return jpaBoardRepository.save(board);}

    @Override
    public Slice<Board> getFilteredAndSortedResults(Long weatherTagId, Long temperatureTagId, Long seasonTagId, Pageable pageable){
        return jpaBoardRepository.getFilteredAndSortedResults(weatherTagId, temperatureTagId, seasonTagId, pageable);
    }


}
