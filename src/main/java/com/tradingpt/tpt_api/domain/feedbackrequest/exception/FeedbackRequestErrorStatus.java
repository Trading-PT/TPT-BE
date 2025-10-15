package com.tradingpt.tpt_api.domain.feedbackrequest.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackRequestErrorStatus implements BaseCodeInterface {

	// 피드백 요청 4000번대 에러
	FEEDBACK_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK4001", "피드백 요청을 찾을 수 없습니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK4002", "접근 권한이 없습니다."),
	DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK4003", "자신의 피드백 요청만 삭제할 수 있습니다."),
	COMPLETED_FEEDBACK_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FEEDBACK4004", "완료된 피드백 요청은 삭제할 수 없습니다."),
	FEEDBACK_RESPONSE_ALREADY_EXISTS(HttpStatus.CONFLICT, "FEEDBACK4005", "이미 답변이 작성된 피드백 요청입니다."),
	FEEDBACK_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK4006", "피드백 답변이 존재하지 않습니다."),
	RESPONSE_UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK4007", "답변 작성자만 수정할 수 있습니다."),
	REQUEST_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "FEEDBACK4008", "요청 날짜가 필수입니다."),
	UNSUPPORTED_REQUEST_FEEDBACK_TYPE(HttpStatus.BAD_REQUEST, "FEEDBACK4009", "지원하지 않는 피드백 타입입니다."),
	BEST_FEEDBACK_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "FEEDBACK4010", "베스트 피드백은 최대 3개까지만 선택할 수 있습니다."),
	FEEDBACK_REQUEST_INVESTMENT_TYPE_MISMATCH(HttpStatus.CONFLICT, "FEEDBACK4011",
		"사용자의 투자 타입과 피드백 요청 타입이 일치하지 않습니다."),
	FEEDBACK_REQUEST_READ_STATUS_CANNOT_BE_NULL(HttpStatus.CONFLICT, "FEEDBACK4012",
		"피드백 요청의 읽음 상태 여부의 개수는 null이면 안됩니다."),
	FEEDBACK_REQUEST_READ_STATUS_CANNOT_BE_MINUS(HttpStatus.CONFLICT, "FEEDBACK4013",
		"피드백 요청의 읽음 상태 여부의 개수는 음수일 수 없습니다."),

	// 트레이딩 계산 관련 에러
	INVALID_WIN_RATE_TOTAL_COUNT_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK4014", "전체 매매 횟수는 null일 수 없습니다."),
	INVALID_WIN_RATE_WIN_COUNT_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK4015", "승리 횟수는 null일 수 없습니다."),
	INVALID_WIN_RATE_TOTAL_COUNT_NEGATIVE(HttpStatus.BAD_REQUEST, "FEEDBACK4016", "전체 매매 횟수는 음수일 수 없습니다."),
	INVALID_WIN_RATE_WIN_COUNT_NEGATIVE(HttpStatus.BAD_REQUEST, "FEEDBACK4017", "승리 횟수는 음수일 수 없습니다."),
	INVALID_WIN_RATE_WIN_GREATER_THAN_TOTAL(HttpStatus.BAD_REQUEST, "FEEDBACK4018", "승리 횟수가 전체 매매 횟수보다 클 수 없습니다."),
	INVALID_RNR_PNL_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK4019", "P&L은 null일 수 없습니다."),
	INVALID_RNR_RISK_TAKING_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK4020", "리스크 테이킹은 null일 수 없습니다."),
	INVALID_RNR_RISK_TAKING_NEGATIVE(HttpStatus.BAD_REQUEST, "FEEDBACK4021", "리스크 테이킹은 음수일 수 없습니다."),

	// ✅ 날짜/기간 검증 관련 에러 추가
	FEEDBACK_PERIOD_MISMATCH(HttpStatus.BAD_REQUEST, "FEEDBACK4022",
		"피드백 연/월/주차 정보가 요청 날짜와 일치하지 않습니다."),
	INVALID_YEAR_MONTH(HttpStatus.BAD_REQUEST, "FEEDBACK4023",
		"연도/월은 현재 또는 과거만 가능합니다."),
	INVALID_DATE(HttpStatus.BAD_REQUEST, "FEEDBACK4024",
		"연도/월/일은 현재 또는 과거만 가능합니다."),

	// ✅ 토큰 관련 에러
	INSUFFICIENT_TOKEN(HttpStatus.BAD_REQUEST, "FEEDBACK_REQUEST_400_20",
		"토큰이 부족합니다."),
	TOKEN_REQUIRED_FOR_BASIC_MEMBERSHIP(HttpStatus.BAD_REQUEST, "FEEDBACK_REQUEST_400_21",
		"BASIC 멤버십은 토큰을 사용해야 피드백 요청이 가능합니다."),
	TOKEN_NOT_ALLOWED_FOR_PREMIUM_MEMBERSHIP(HttpStatus.BAD_REQUEST, "FEEDBACK_REQUEST_400_22",
		"PREMIUM 멤버십은 토큰을 사용할 수 없습니다."),
	CANNOT_RESPOND_TO_NON_TOKEN_FEEDBACK_AS_UNASSIGNED_TRAINER(HttpStatus.FORBIDDEN, "FEEDBACK_REQUEST_403_1",
		"배정되지 않은 트레이너는 토큰 사용 피드백에만 응답할 수 있습니다."),
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