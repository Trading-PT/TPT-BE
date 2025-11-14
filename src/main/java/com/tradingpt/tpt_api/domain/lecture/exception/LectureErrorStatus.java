package com.tradingpt.tpt_api.domain.lecture.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LectureErrorStatus implements BaseCodeInterface {

    NOT_FOUND(HttpStatus.NOT_FOUND, "LECTURE404", "해당 강의를 찾을 수 없습니다."),
    DELETE_FAILED(HttpStatus.BAD_REQUEST, "LECTURE400", "강의 삭제 중 오류가 발생했습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "LECTURE401", "유효하지 않은 강의 공개범위(category) 입니다."),

    // ------------------ 구매 관련 에러 ------------------
    ALREADY_FREE_LECTURE(HttpStatus.BAD_REQUEST, "LECTURE402", "유료 강의는 구매할 수 없습니다."),
    ALREADY_PURCHASED(HttpStatus.BAD_REQUEST, "LECTURE403", "이미 구매한 강의입니다."),
    NOT_ENOUGH_TOKENS(HttpStatus.BAD_REQUEST, "LECTURE404", "보유 토큰이 부족합니다.");

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
