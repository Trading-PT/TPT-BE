package com.tradingpt.tpt_api.domain.investmenttypehistory.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 투자 유형 이력 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: INVEST_HIST_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum InvestmentHistoryErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	INVESTMENT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INVEST_HIST_404_0", "트레이딩 유형이 확인되지 않습니다."),
	CHANGE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "INVEST_HIST_404_1", "변경 신청을 찾을 수 없습니다."),

	// 403 Forbidden
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "INVEST_HIST_403_0", "해당 신청에 대한 권한이 없습니다."),

	// 400 Bad Request
	SAME_INVESTMENT_TYPE(HttpStatus.BAD_REQUEST, "INVEST_HIST_400_0", "현재와 동일한 투자 유형으로는 변경할 수 없습니다."),
	PENDING_REQUEST_EXISTS(HttpStatus.BAD_REQUEST, "INVEST_HIST_400_1", "이미 대기 중인 변경 신청이 있습니다."),
	REQUEST_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "INVEST_HIST_400_2", "이미 처리된 신청입니다."),
	INVESTMENT_HISTORY_TYPE_CHANGE_CAN_BE_PROCEEDED_AT_FIRST_DATE(HttpStatus.BAD_REQUEST, "INVEST_HIST_400_3", "투자 타입 변경은 매월 1일에만 가능합니다."),
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
