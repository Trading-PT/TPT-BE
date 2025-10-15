package com.tradingpt.tpt_api.domain.weeklytradingsummary.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class WeeklyTradingSummaryException extends BaseException {
	/**
	 * BaseException 생성자
	 * RuntimeException에 에러 메시지를 전달하여 로그에서 의미 있는 메시지 출력
	 *
	 * @param errorCode
	 */
	public WeeklyTradingSummaryException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}
