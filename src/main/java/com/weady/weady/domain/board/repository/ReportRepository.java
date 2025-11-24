package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.Report;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByBoardAndUser(Board board, User user);


}
