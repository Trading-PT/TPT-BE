package com.tradingpt.tpt_api.domain.monthlytradingsummary.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class MonthlyTradingSummaryException extends BaseException {
	/**
	 * BaseException 생성자
	 * RuntimeException에 에러 메시지를 전달하여 로그에서 의미 있는 메시지 출력
	 *
	 * @param errorCode
	 */
	public MonthlyTradingSummaryException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}
