package com.tradingpt.tpt_api.domain.monthlytradingsummary.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MonthlyTradingSummaryErrorStatus implements BaseCodeInterface {

	// 400 Bad Request
	MONTHLY_SUMMARY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MONTHLY_SUMMARY4001",
		"해당 연/월에 대한 월간 요약이 이미 존재합니다."),
	COURSE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "MONTHLY_SUMMARY4002",
		"해당 연/월에 완강 후 상태의 피드백이 없습니다. 완강 후에만 월간 요약을 작성할 수 있습니다."),
	INVALID_INVESTMENT_TYPE(HttpStatus.BAD_REQUEST, "MONTHLY_SUMMARY4003",
		"월간 요약은 데이 트레이딩 또는 스윙 트레이딩 타입에서만 작성할 수 있습니다."),

	// 404 Not Found
	MONTHLY_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "MONTHLY_SUMMARY4041",
		"월간 요약을 찾을 수 없습니다.");

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