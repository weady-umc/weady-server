package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaBoardRepository extends JpaRepository<Board, Long> {



}
