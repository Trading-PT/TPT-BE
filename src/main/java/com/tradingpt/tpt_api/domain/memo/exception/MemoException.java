package com.tradingpt.tpt_api.domain.memo.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 메모 도메인 전용 예외 클래스
 * 메모 관련 비즈니스 로직에서 발생하는 예외를 처리
 */
public class MemoException extends BaseException {
    public MemoException(BaseCodeInterface errorCode) {
        super(errorCode);
    }
}
