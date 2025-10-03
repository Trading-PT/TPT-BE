package com.tradingpt.tpt_api.domain.investmenthistory.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvestmentHistoryErrorStatus implements BaseCodeInterface {

	INVESTMENT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INVESTMENT_HISTORY4001", "트레이딩 유형이 확인되지 않습니다."),
	;

	private final HttpStatus httpStatus;
	private final boolean isSuccess = false;
	private final String code;
	private final String message;

	@Override
	public BaseCode getCode() {
		return BaseCode.builder()
			.httpStatus(httpStatus)
			.isSuccess(isSuccess)
			.code(code)
			.message(message)
			.build();
	}
}
