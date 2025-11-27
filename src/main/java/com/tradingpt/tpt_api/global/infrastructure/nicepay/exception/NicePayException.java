package com.tradingpt.tpt_api.global.infrastructure.nicepay.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;

/**
 * NicePay API 관련 예외 클래스
 */
public class NicePayException extends BaseException {

    private final NicePayErrorStatus errorStatus;

    public NicePayException(NicePayErrorStatus errorStatus) {
        super(errorStatus);
        this.errorStatus = errorStatus;
    }

    /**
     * NicePay 에러 상태 반환
     * @return NicePayErrorStatus 에러 상태
     */
    public NicePayErrorStatus getErrorStatus() {
        return errorStatus;
    }

    /**
     * 일시적 오류 여부 확인
     * @return 일시적 오류이면 true
     */
    public boolean isTemporaryError() {
        return errorStatus == NicePayErrorStatus.TEMPORARY_ERROR;
    }
}
