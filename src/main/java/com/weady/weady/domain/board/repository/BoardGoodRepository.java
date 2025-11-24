package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardGood;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardGoodRepository extends JpaRepository<BoardGood, Long> {

    boolean existsByBoardAndUser(Board board, User user);

    void deleteByBoardAndUser(Board board, User user);

}
