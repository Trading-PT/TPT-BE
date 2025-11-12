package com.tradingpt.tpt_api.domain.memo.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 메모 도메인 에러 상태 코드 정의
 * 메모 관련 비즈니스 로직에서 발생할 수 있는 모든 에러 상태를 정의
 */
@Getter
@AllArgsConstructor
public enum MemoErrorStatus implements BaseCodeInterface {

    // 메모 6000번대 에러
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO6001", "메모를 찾을 수 없습니다."),
    MEMO_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMO6002", "이미 메모가 존재합니다."),
    MEMO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEMO6003", "메모에 접근 권한이 없습니다."),
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
