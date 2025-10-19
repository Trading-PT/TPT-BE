package com.tradingpt.tpt_api.domain.leveltest.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LeveltestErrorStatus implements BaseCodeInterface {

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVELTEST4001", "해당 문제를 찾을 수 없습니다."),
    ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVELTEST4002", "해당 시도를 찾을 수 없습니다."),
    ATTEMPT_NOT_ALLOWED(HttpStatus.FORBIDDEN, "LEVELTEST4003", "레벨테스트는 완강 후에만 재시도할 수 있습니다."),
    RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVELTEST4004", "해당 응답을 찾을 수 없습니다."),
    RESPONSE_NOT_IN_ATTEMPT(HttpStatus.BAD_REQUEST, "LEVELTEST4005", "응답이 해당 시도에 포함되어 있지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "LEVELTEST4006", "잘못된 요청 형식입니다.");

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
