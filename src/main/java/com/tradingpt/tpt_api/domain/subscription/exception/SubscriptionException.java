package com.tradingpt.tpt_api.domain.subscription.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 구독 도메인 전용 예외 클래스
 * 구독 관련 비즈니스 로직에서 발생하는 예외를 처리
 */
public class SubscriptionException extends BaseException {
    public SubscriptionException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
