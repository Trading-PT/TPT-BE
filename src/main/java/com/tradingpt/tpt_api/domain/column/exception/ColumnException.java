package com.tradingpt.tpt_api.domain.column.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class ColumnException extends BaseException {
    public ColumnException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
