package com.tradingpt.tpt_api.domain.consultation.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class ConsultationException extends BaseException {
    public ConsultationException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
