package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.comment.BoardComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<BoardComment, Long> {

    // 부모 댓글만 슬라이싱
    @Query("SELECT bc FROM BoardComment bc "
            + "JOIN FETCH bc.user "
            + "WHERE bc.board = :board AND bc.parentComment IS NULL "
            + "ORDER BY bc.createdAt ASC ")
    Slice<BoardComment> findParentsByBoard(Board board, Pageable pageable);

    // 자식 댓글은 리스트로 조회
    @Query("SELECT bc FROM BoardComment bc "
            + "JOIN FETCH bc.user "
            + "WHERE bc.parentComment.id IN :parentIds "
            + "ORDER BY bc.parentComment.id ASC, bc.createdAt ASC, bc.id ASC ")
    List<BoardComment> findChildrenByParentIds(@Param("parentIds") List<Long> parentIds);

    int deleteAllByParentComment(BoardComment parentComment);

}
