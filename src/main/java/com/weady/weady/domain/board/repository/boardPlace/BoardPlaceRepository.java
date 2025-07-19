package com.weady.weady.domain.board.repository.boardPlace;

import com.weady.weady.domain.board.entity.board.BoardPlace;

import java.util.List;
import java.util.Optional;

public interface BoardPlaceRepository {
    List<BoardPlace> saveAll(List<BoardPlace> boardPlaces);
    Optional<BoardPlace> findById(Long id);
}
