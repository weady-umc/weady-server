package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


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
            @Param("seasonTagId") Long seasonTagId,
            @Param("temperatureTagId") Long temperatureTagId,
            @Param("weatherTagId") Long weatherTagId,
            @Param("userId") Long userId,
            Pageable pageable);


    @Query("""
        SELECT b
        FROM Board b
        WHERE b.user.id = :userId
          AND FUNCTION('YEAR', b.createdAt) = :year
          AND FUNCTION('MONTH', b.createdAt) = :month
        ORDER BY b.createdAt ASC
    """)
    List<Board> findBoardsByUserIdAndYearMonth(@Param("userId") Long userId,
                                               @Param("year") int year,
                                               @Param("month") int month);


    @Query("""
    SELECT b FROM Board b
    WHERE b.user.id = :userId
      AND b.createdAt BETWEEN :start AND :end
      AND b.isPublic = :isPublic
""")
    Optional<Board> findBoardByUserIdAndCreatedAtBetweenAndIsPublic(@Param("userId") Long userId,
                                                                    @Param("start") LocalDateTime start,
                                                                    @Param("end") LocalDateTime end,
                                                                    @Param("isPublic") boolean isPublic);


}