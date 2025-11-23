package com.tradingpt.tpt_api.domain.event.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventErrorStatus implements BaseCodeInterface {

    // 404 Not Found
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVENT_404_0", "해당 이벤트를 찾을 수 없습니다."),

    // 400 Bad Request
    INVALID_EVENT_PERIOD(HttpStatus.BAD_REQUEST, "EVENT_400_0", "이벤트 시작/종료 일시가 유효하지 않습니다."),
    INVALID_TOKEN_AMOUNT(HttpStatus.BAD_REQUEST, "EVENT_400_1", "발급 토큰 개수는 1 이상이어야 합니다."),

    // 403 Forbidden
    EVENT_MODIFICATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "EVENT_403_0", "이미 종료된 이벤트는 수정할 수 없습니다."),
    ;

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
