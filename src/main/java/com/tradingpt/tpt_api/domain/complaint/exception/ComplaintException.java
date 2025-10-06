package com.tradingpt.tpt_api.domain.complaint.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class ComplaintException extends BaseException {
    public ComplaintException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
