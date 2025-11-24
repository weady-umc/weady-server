package com.weady.weady.domain.board.mapper;

import com.weady.weady.domain.board.dto.response.CommentResponseDto;
import com.weady.weady.domain.board.dto.response.CommentWithChildResponseDto;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.comment.BoardComment;
import com.weady.weady.domain.user.entity.User;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {

    public static BoardComment toBoardComment(BoardComment boardComment, User user, Board board, String content) {

        return BoardComment.builder()
                .parentComment(boardComment)
                .board(board)
                .user(user)
                .content(content)
                .build();
    }


    public static CommentResponseDto toCommentResponseDto(BoardComment boardComment) {

        Long parentId = null;
        if(boardComment.getParentComment() != null) {
            parentId = boardComment.getParentComment().getId();
        }

        return CommentResponseDto.builder()
                .commentId(boardComment.getId())
                .parentId(parentId)
                .username(boardComment.getUser().getName())
                .profileImageUrl(boardComment.getUser().getProfileImageUrl())
                .content(boardComment.getContent())
                .createdAt(boardComment.getCreatedAt())
                .build();
    }


    public static CommentWithChildResponseDto toCommentWithChildResponseDto(BoardComment boardComment, List<BoardComment> childComments) {

        Long parentId = null;
        if(boardComment.getParentComment() != null) {
            parentId = boardComment.getParentComment().getId();
        }

        return CommentWithChildResponseDto.builder()
                .commentId(boardComment.getId())
                .parentId(parentId)
                .username(boardComment.getUser().getName())
                .profileImageUrl(boardComment.getUser().getProfileImageUrl())
                .content(boardComment.getContent())
                .childCommentsList(childComments.stream()
                        .map(CommentMapper::toCommentResponseDto)
                        .collect(Collectors.toList()))
                .createdAt(boardComment.getCreatedAt())

                .build();
    }

}
