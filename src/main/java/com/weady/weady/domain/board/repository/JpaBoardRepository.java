package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface JpaBoardRepository extends JpaRepository<Board, Long> {

}
