package com.tradingpt.tpt_api.global.exception.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 기본 응답 코드 클래스
 * HTTP 상태 코드, 성공 여부, 코드, 메시지를 담는 클래스
 */
@Getter
@Builder
public class BaseCode {
    private final HttpStatus httpStatus;
    private final boolean isSuccess;
    private final String code;
    private final String message;
}
