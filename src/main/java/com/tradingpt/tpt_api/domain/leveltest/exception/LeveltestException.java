package com.tradingpt.tpt_api.domain.leveltest.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class LeveltestException extends BaseException {
    public LeveltestException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
