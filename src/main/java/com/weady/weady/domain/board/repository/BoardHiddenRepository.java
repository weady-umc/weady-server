package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.BoardHidden;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardHiddenRepository extends JpaRepository<BoardHidden, Long> {

    boolean existsByBoardAndUser(Board board, User user);
    void deleteByBoardAndUser(Board board, User user);
}
