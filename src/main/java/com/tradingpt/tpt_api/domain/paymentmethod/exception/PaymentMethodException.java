package com.tradingpt.tpt_api.domain.paymentmethod.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * PaymentMethod 도메인 예외
 */
public class PaymentMethodException extends BaseException {

    public PaymentMethodException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
