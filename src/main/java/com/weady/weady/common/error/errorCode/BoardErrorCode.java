package com.weady.weady.common.error.errorCode;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardErrorCode implements ErrorCode {

    //보드 관련
    BOARD_NOT_FOUND(404, "해당 게시글을 찾을 수 없습니다."),
    BOARD_GOOD_NOT_FOUND(404, "좋아요 기록을 찾을 수 없습니다."),
    ALREADY_LIKED(409, "이미 좋아요 한 게시물입니다."),
    UNAUTHORIZED_UPDATE(403, "게시물 수정 권한이 없습니다."),
    UNAUTHORIZED_DELETE(403, "삭제 권한이 없습니다."),
    WEADYCHIVE_BOARD_NOT_FOUND(404, "스크랩 기록을 찾을 수 없습니다."),
    ALREADY_SCRAPED(409, "이미 스크랩 한 게시물입니다."),
    BOARD_HIDDEN_NOT_FOUND(404, "숨김 기록을 찾을 수 없습니다."),
    ALREADY_HIDDEN(409, "이미 숨김 처리된 게시물입니다."),
    ALREADY_REPORTED(409, "이미 신고 처리된 게시물입니다."),


    //댓글 관련
    COMMENT_NOT_FOUND(404, "해당 댓글을 찾을 수 없습니다."),

    ;

    private final int code;
    private final String message;
}
