package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaBoardRepository extends JpaRepository<Board, Long> {


    @Query("SELECT b "
            + "FROM Board b "
            + "WHERE b.isPublic = true "
            + "AND (:weatherTagId IS NULL OR b.weatherTag.id = :weatherTagId) "
            + "AND (:temperatureTagId IS NULL OR b.temperatureTag.id = :temperatureTagId) "
            + "AND (:seasonTagId IS NULL OR b.seasonTag.id = :seasonTagId) "
            + "ORDER BY b.createdAt DESC, b.id DESC")
    Slice<Board> getFilteredAndSortedResults(
            @Param("weatherTagId") Long weatherTagId,
            @Param("temperatureTagId") Long temperatureTagId,
            @Param("seasonTagId") Long seasonTagId,
            Pageable pageable);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.goodCount = b.goodCount + 1 WHERE b.id = :id")
    void increaseGoodCount(@Param("id") Long boardId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.goodCount = b.goodCount - 1 WHERE b.id = :id")
    void decreaseGoodCount(@Param("id") Long boardId);


}
