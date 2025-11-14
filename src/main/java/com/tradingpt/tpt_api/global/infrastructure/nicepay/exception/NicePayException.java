package com.tradingpt.tpt_api.global.infrastructure.nicepay.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * NicePay API 관련 예외 클래스
 */
public class NicePayException extends BaseException {

    public NicePayException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
