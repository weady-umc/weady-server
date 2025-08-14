package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.BoardImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardImgRepository extends JpaRepository<BoardImg, Long> {
    @Query("""
        SELECT i.imgUrl
        FROM BoardImg i
        WHERE i.board.id = :boardId
          AND i.imgOrder = 1
    """)
    Optional<String> findThumbnailUrlByBoardId(@Param("boardId") Long boardId);

    @Query("""
        SELECT i FROM BoardImg i
        WHERE i.board.id = :boardId
        ORDER BY i.imgOrder ASC
    """)
    List<BoardImg> findAllByBoardIdOrderByImgOrderAsc(@Param("boardId") Long boardId);
}
