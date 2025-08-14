package com.weady.weady.domain.board.service;


import com.weady.weady.common.error.errorCode.BoardErrorCode;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import com.weady.weady.domain.board.dto.request.CommentCreateRequestDto;
import com.weady.weady.domain.board.dto.response.CommentResponseDto;
import com.weady.weady.domain.board.dto.response.CommentWithChildResponseDto;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.comment.BoardComment;
import com.weady.weady.domain.board.mapper.BoardMapper;
import com.weady.weady.domain.board.mapper.CommentMapper;
import com.weady.weady.domain.board.repository.BoardRepository;
import com.weady.weady.domain.board.repository.CommentRepository;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;



    /**
     * 1. 전체 댓글 조회
     * @return Slice<CommentWithChildResponseDto>
     * @thorws
     */
    @Transactional(readOnly = true)
    public Slice<CommentWithChildResponseDto> getComments(Long boardId, Integer size) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, size);
        Slice<BoardComment> parentComments = commentRepository.findByBoardAndParentCommentIsNull(board, pageable);

        // 부모 댓글과 자식 댓글을 매핑
        List<CommentWithChildResponseDto> commentResponses = parentComments.stream()
                .map(parentComment -> {
                    List<BoardComment> childComments = commentRepository.findAllByParentCommentId(parentComment.getId());
                    return CommentMapper.toCommentWithChildResponseDto(parentComment, childComments);
                })
                .collect(Collectors.toList());
        return new SliceImpl<>(commentResponses);
    }

    /**
     * 2. 댓글 및 대댓글 작성
     * @return CommentResponseDto
     * @thorws
     */
    @Transactional
    public CommentResponseDto createComment(CommentCreateRequestDto requestDto, Long boardId) {

        // 댓글 작성자 정보 조회
        User user = userRepository.findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.BOARD_NOT_FOUND));

        BoardComment parentComment = null;

        if(requestDto.parentId() != null) {
            parentComment = commentRepository.findById(requestDto.parentId())
                    .orElseThrow(() -> new BusinessException(BoardErrorCode.COMMENT_NOT_FOUND));

            // 대댓글의 대댓글일 경우 -> 최상위 부모 댓글의 자식 댓글로 지정
            if (parentComment.getParentComment() != null) {
                parentComment = parentComment.getParentComment();
            }
        }

        BoardComment newBoardComment = CommentMapper.toBoardComment(parentComment, user, board, requestDto.content());

        // 데이터 저장
        commentRepository.save(newBoardComment);

        board.increaseCommentCount();

        return CommentMapper.toCommentResponseDto(newBoardComment);

    }


    /**
     * 4. 댓글 삭제
     * @return
     * @thorws
     */
    @Transactional
    public void deleteComment(Long commentId) {

        BoardComment boardComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(BoardErrorCode.COMMENT_NOT_FOUND));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        Long commentAuthorId = boardComment.getUser().getId();
        Long postAuthorId = boardComment.getBoard().getUser().getId();

        if (Objects.equals(commentAuthorId, currentUserId) || Objects.equals(postAuthorId, currentUserId)) {
            //자식 댓글 먼저 삭제 후 부모 댓글 삭제
            int children = commentRepository.deleteAllByParentComment(boardComment);
            commentRepository.delete(boardComment);
            Board board = boardComment.getBoard();
            board.decreaseCommentCount(children);
            return;
        }
        throw new BusinessException(BoardErrorCode.UNAUTHORIZED_DELETE);
    }
}
