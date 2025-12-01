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

    /**
     * 상세 메시지를 포함하는 생성자
     * PG사 에러 등 외부 시스템의 구체적인 에러 메시지를 사용자에게 전달하기 위해 사용
     *
     * @param errorCode     에러 코드
     * @param customMessage 커스텀 에러 메시지 (사용자에게 표시될 메시지)
     */
    public PaymentMethodException(BaseCodeInterface errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
