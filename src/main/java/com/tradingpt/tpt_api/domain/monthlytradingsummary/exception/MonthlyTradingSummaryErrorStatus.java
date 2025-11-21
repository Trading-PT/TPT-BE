package com.tradingpt.tpt_api.domain.monthlytradingsummary.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 월간 매매 요약 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: MONTHLY_SUM_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum MonthlyTradingSummaryErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	MONTHLY_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "MONTHLY_SUM_404_0",
		"월간 요약을 찾을 수 없습니다."),

	// 400 Bad Request
	MONTHLY_SUMMARY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MONTHLY_SUM_400_0",
		"해당 연/월에 대한 월간 요약이 이미 존재합니다."),
	COURSE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "MONTHLY_SUM_400_1",
		"해당 연/월에 완강 후 상태의 피드백이 없습니다. 완강 후에만 월간 요약을 작성할 수 있습니다."),
	INVALID_INVESTMENT_TYPE(HttpStatus.BAD_REQUEST, "MONTHLY_SUM_400_2",
		"월간 요약은 데이 트레이딩 또는 스윙 트레이딩 타입에서만 작성할 수 있습니다."),
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
