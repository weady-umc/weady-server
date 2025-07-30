package com.weady.weady.domain.board.repository;

import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.comment.BoardComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<BoardComment, Long> {
    // 부모 댓글만 페이징 조회
    Slice<BoardComment> findByBoardAndParentCommentIsNull(Board board, Pageable pageable);

    // 자식 댓글은 부모 ID 기준. 작성된 순서로 정렬된 리스트로 조회
    List<BoardComment> findAllByParentCommentId(Long parentId);


    void deleteAllByParentComment(BoardComment parentComment);

}
