package com.tradingpt.tpt_api.domain.lecture.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class LectureException extends BaseException {
    public LectureException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
