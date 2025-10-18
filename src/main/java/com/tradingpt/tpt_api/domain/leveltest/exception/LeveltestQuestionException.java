package com.tradingpt.tpt_api.domain.leveltest.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class LeveltestQuestionException extends BaseException {
    public LeveltestQuestionException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
