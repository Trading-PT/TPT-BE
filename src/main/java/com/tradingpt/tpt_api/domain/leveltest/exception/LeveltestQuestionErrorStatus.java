package com.tradingpt.tpt_api.domain.leveltest.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LeveltestQuestionErrorStatus implements BaseCodeInterface {

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVELTESTQUESTSION4001", "해당 문제를 찾을 수 없습니다.");

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
