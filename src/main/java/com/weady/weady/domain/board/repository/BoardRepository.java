package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b "
            + "FROM Board b "
            + "WHERE b.isPublic = true "
            + "AND (:weatherTagId IS NULL OR b.weatherTag.id = :weatherTagId) "
            + "AND (:temperatureTagId IS NULL OR b.temperatureTag.id = :temperatureTagId) "
            + "AND (:seasonTagId IS NULL OR b.seasonTag.id = :seasonTagId) "
            + "AND b.id NOT IN ("
            + "    SELECT bh.board.id FROM BoardHidden bh WHERE bh.user.id = :userId" // 숨긴 게시물 제외
            + ") "
            + "ORDER BY b.createdAt DESC, b.id DESC")
    Slice<Board> getFilteredAndSortedResults(
            @Param("weatherTagId") Long weatherTagId,
            @Param("temperatureTagId") Long temperatureTagId,
            @Param("seasonTagId") Long seasonTagId,
            @Param("userId") Long userId,
            Pageable pageable);

}


