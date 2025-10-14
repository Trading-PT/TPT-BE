package com.tradingpt.tpt_api.domain.investmenttypehistory.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvestmentHistoryErrorStatus implements BaseCodeInterface {

	INVESTMENT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INVESTMENT_HISTORY4041", "트레이딩 유형이 확인되지 않습니다."),

	// 변경 신청 관련
	SAME_INVESTMENT_TYPE(HttpStatus.BAD_REQUEST, "INVESTMENT_CHANGE_400_1",
		"현재와 동일한 투자 유형으로는 변경할 수 없습니다."),
	PENDING_REQUEST_EXISTS(HttpStatus.BAD_REQUEST, "INVESTMENT_CHANGE_400_2",
		"이미 대기 중인 변경 신청이 있습니다."),
	CHANGE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "INVESTMENT_CHANGE_404_1",
		"변경 신청을 찾을 수 없습니다."),
	REQUEST_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "INVESTMENT_CHANGE_400_3",
		"이미 처리된 신청입니다."),
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "INVESTMENT_CHANGE_403_1",
		"해당 신청에 대한 권한이 없습니다."),
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
