package com.tradingpt.tpt_api.domain.subscription.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 구독 도메인 에러 상태 코드 정의
 * 구독 관련 비즈니스 로직에서 발생할 수 있는 모든 에러 상태를 정의
 *
 * 에러 코드 형식: SUBSCRIPT_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum SubscriptionErrorStatus implements BaseCodeInterface {

    // 503 Service Unavailable (Temporary Error - Retryable)
    FIRST_PAYMENT_TEMPORARY_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "SUBSCRIPT_503_0", "결제가 일시적으로 실패했습니다. 잠시 후 다시 시도해주세요."),

    // 500 Internal Server Error
    SUBSCRIPTION_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUBSCRIPT_500_0", "구독 정보 업데이트에 실패했습니다."),
    FIRST_PAYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUBSCRIPT_500_1", "첫 번째 결제에 실패했습니다. 카드 정보를 확인하고 다시 시도해주세요."),

    // 409 Conflict
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "SUBSCRIPT_409_0", "이미 활성화된 구독이 존재합니다."),

    // 404 Not Found
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPT_404_0", "구독 정보를 찾을 수 없습니다."),
    SUBSCRIPTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPT_404_1", "구독 플랜을 찾을 수 없습니다."),
    ACTIVE_SUBSCRIPTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPT_404_2", "활성화된 구독 플랜을 찾을 수 없습니다."),

    // 400 Bad Request
    SUBSCRIPTION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "SUBSCRIPT_400_0", "이미 해지된 구독입니다."),
    SUBSCRIPTION_PAYMENT_FAILED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SUBSCRIPT_400_1", "결제 실패 횟수가 한도를 초과했습니다."),
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
