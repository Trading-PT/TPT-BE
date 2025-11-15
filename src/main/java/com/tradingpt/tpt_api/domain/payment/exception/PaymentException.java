package com.tradingpt.tpt_api.domain.payment.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 결제 도메인 전용 예외 클래스
 * 결제 관련 비즈니스 로직에서 발생하는 예외를 처리
 */
public class PaymentException extends BaseException {
    public PaymentException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
