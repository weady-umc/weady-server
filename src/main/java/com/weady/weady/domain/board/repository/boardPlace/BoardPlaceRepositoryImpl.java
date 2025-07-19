package com.weady.weady.domain.board.repository.boardPlace;

import com.weady.weady.domain.board.entity.board.BoardPlace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardPlaceRepositoryImpl implements BoardPlaceRepository {

    private final JpaBoardPlaceRepository jpaBoardPlaceRepository;

    @Override
    public List<BoardPlace> saveAll(List<BoardPlace> boardPlaces) {
        return jpaBoardPlaceRepository.saveAll(boardPlaces);
    }

    @Override
    public Optional<BoardPlace> findById(Long id) {
        return jpaBoardPlaceRepository.findById(id);
    }
}
