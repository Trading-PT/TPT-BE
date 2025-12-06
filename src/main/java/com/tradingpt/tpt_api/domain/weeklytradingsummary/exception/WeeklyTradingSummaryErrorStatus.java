package com.tradingpt.tpt_api.domain.weeklytradingsummary.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 주간 매매 요약 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: WEEKLY_SUM_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9, 2자리 확장 가능)
 */
@Getter
@AllArgsConstructor
public enum WeeklyTradingSummaryErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	WEEKLY_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "WEEKLY_SUM_404_0",
		"주간 요약을 찾을 수 없습니다."),

	// 400 Bad Request
	WEEKLY_SUMMARY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_0",
		"해당 연/월/주에 대한 주간 요약이 이미 존재합니다."),
	TRAINER_CANNOT_CREATE_FOR_BEFORE_COMPLETION(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_1",
		"완강 전에는 트레이너가 주간 요약을 작성할 수 없습니다. 고객이 memo를 작성해야 합니다."),
	DETAILED_EVALUATION_INCOMPLETE(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_2",
		"완강 후 + DAY 유형에서는 weeklyEvaluation, weeklyProfitableTradingAnalysis, weeklyLossTradingAnalysis를 모두 작성해야 합니다."),
	MEMO_NOT_ALLOWED_FOR_TRAINER_AFTER_COMPLETION(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_3",
		"완강 후에는 트레이너가 memo를 작성할 수 없습니다."),
	TRAINER_CANNOT_CREATE_FOR_NON_DAY_AFTER_COMPLETION(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_4",
		"완강 후 SWING 유형에서는 주간 요약을 작성할 수 없습니다."),
	CUSTOMER_CANNOT_CREATE_FOR_AFTER_COMPLETION(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_5",
		"완강 후에는 고객이 주간 요약을 작성할 수 없습니다."),
	MEMO_REQUIRED_FOR_CUSTOMER_BEFORE_COMPLETION(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_6",
		"완강 전에는 memo를 작성해야 합니다."),
	DETAILED_EVALUATION_NOT_ALLOWED_FOR_CUSTOMER(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_7",
		"고객은 상세 평가를 작성할 수 없습니다."),
	MEMBERSHIP_NOT_PREMIUM(HttpStatus.BAD_REQUEST, "WEEKLY_SUM_400_8",
		"PREMIUM 멤버십 고객만 트레이너 평가 작성 대상입니다."),
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
