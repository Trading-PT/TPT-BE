package com.tradingpt.tpt_api.domain.column.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ColumnErrorStatus implements BaseCodeInterface {

    NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN4001", "해당 칼럼을 찾을 수 없습니다."),

    // 작성자 관련 에러
    WRITER_NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN4002", "작성자를 찾을 수 없습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "COLUMN4003", "유효하지 않은 사용자 역할입니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "COLUMN4004", "작성자 본인만 수정할 수 있습니다."),

    // 기타 권한/상태 관련 에러
    DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "COLUMN4005", "칼럼 삭제 권한이 없습니다."),
    DUPLICATE_TITLE(HttpStatus.CONFLICT, "COLUMN4006", "동일한 제목의 칼럼이 이미 존재합니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN4007", "해당 카테고리를 찾을 수 없습니다"),
    LIKE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COLUMN4008", "좋아요 수는 10,000을 초과할 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN4008", "해당 댓글을 찾을 수 없습니다");

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCode getCode() {
        return BaseCode.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
}
