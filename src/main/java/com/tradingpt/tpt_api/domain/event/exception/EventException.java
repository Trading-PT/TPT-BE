package com.tradingpt.tpt_api.domain.event.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class EventException extends BaseException {
    public EventException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
