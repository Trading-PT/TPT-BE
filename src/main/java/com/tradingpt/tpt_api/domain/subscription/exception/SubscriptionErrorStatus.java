package com.tradingpt.tpt_api.domain.subscription.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 구독 도메인 에러 상태 코드 정의
 * 구독 관련 비즈니스 로직에서 발생할 수 있는 모든 에러 상태를 정의
 */
@Getter
@AllArgsConstructor
public enum SubscriptionErrorStatus implements BaseCodeInterface {

    // 구독 9000번대 에러
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION9001", "구독 정보를 찾을 수 없습니다."),
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "SUBSCRIPTION9002", "이미 활성화된 구독이 존재합니다."),
    SUBSCRIPTION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "SUBSCRIPTION9003", "이미 해지된 구독입니다."),
    SUBSCRIPTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION9004", "구독 플랜을 찾을 수 없습니다."),
    ACTIVE_SUBSCRIPTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION9005", "활성화된 구독 플랜을 찾을 수 없습니다."),
    SUBSCRIPTION_PAYMENT_FAILED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SUBSCRIPTION9006", "결제 실패 횟수가 한도를 초과했습니다."),
    SUBSCRIPTION_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SUBSCRIPTION9007", "구독 정보 업데이트에 실패했습니다."),
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
