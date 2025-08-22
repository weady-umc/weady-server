package com.weady.weady.domain.weadychive.repository.board;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.weadychive.entity.WeadychiveBoard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeadychiveBoardRepository extends JpaRepository<WeadychiveBoard, Long> {
    @Query("SELECT b FROM WeadychiveBoard wb " +
            "JOIN wb.board b " +
            "JOIN FETCH b.user u " +
            "WHERE wb.user.id = :userId ORDER BY wb.createdAt DESC")
    Slice<Board> findBoardsScrappedByUserId(@Param("userId") Long userId, Pageable pageable);


    Boolean existsByUserAndBoard(User user, Board board);
    void deleteByUserAndBoard(User user, Board board);

}
