package com.tradingpt.tpt_api.domain.feedbackrequest.exception;

import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest.*;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 피드백 요청 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: FEEDBACK_REQ_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 403, 404, 409)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9, 2자리 확장 00-99)
 */
@Getter
@AllArgsConstructor
public enum FeedbackRequestErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	FEEDBACK_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK_REQ_404_0", "피드백 요청을 찾을 수 없습니다."),
	FEEDBACK_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK_REQ_404_1", "피드백 답변이 존재하지 않습니다."),

	// 403 Forbidden
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK_REQ_403_0", "접근 권한이 없습니다."),
	DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK_REQ_403_1", "자신의 피드백 요청만 삭제할 수 있습니다."),
	RESPONSE_UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK_REQ_403_2", "답변 작성자만 수정할 수 있습니다."),
	CANNOT_RESPOND_TO_NON_TOKEN_FEEDBACK_AS_UNASSIGNED_TRAINER(HttpStatus.FORBIDDEN, "FEEDBACK_REQ_403_3", "배정되지 않은 트레이너는 토큰 사용 피드백에만 응답할 수 있습니다."),
	UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK_REQ_403_4", "자신의 피드백 요청만 수정할 수 있습니다."),

	// 409 Conflict
	FEEDBACK_RESPONSE_ALREADY_EXISTS(HttpStatus.CONFLICT, "FEEDBACK_REQ_409_0", "이미 답변이 작성된 피드백 요청입니다."),
	FEEDBACK_REQUEST_INVESTMENT_TYPE_MISMATCH(HttpStatus.CONFLICT, "FEEDBACK_REQ_409_1", "사용자의 투자 타입과 피드백 요청 타입이 일치하지 않습니다."),
	FEEDBACK_REQUEST_READ_STATUS_CANNOT_BE_NULL(HttpStatus.CONFLICT, "FEEDBACK_REQ_409_2", "피드백 요청의 읽음 상태 여부의 개수는 null이면 안됩니다."),
	FEEDBACK_REQUEST_READ_STATUS_CANNOT_BE_MINUS(HttpStatus.CONFLICT, "FEEDBACK_REQ_409_3", "피드백 요청의 읽음 상태 여부의 개수는 음수일 수 없습니다."),

	// 400 Bad Request
	COMPLETED_FEEDBACK_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_00", "완료된 피드백 요청은 삭제할 수 없습니다."),
	REQUEST_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_01", "요청 날짜가 필수입니다."),
	UNSUPPORTED_REQUEST_FEEDBACK_TYPE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_02", "지원하지 않는 피드백 타입입니다."),
	BEST_FEEDBACK_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_03", "베스트 피드백은 최대 " + MAX_BEST_FEEDBACK_COUNT + "개까지만 선택할 수 있습니다."),
	INVALID_WIN_RATE_TOTAL_COUNT_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_04", "전체 매매 횟수는 null일 수 없습니다."),
	INVALID_WIN_RATE_WIN_COUNT_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_05", "승리 횟수는 null일 수 없습니다."),
	INVALID_WIN_RATE_TOTAL_COUNT_NEGATIVE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_06", "전체 매매 횟수는 음수일 수 없습니다."),
	INVALID_WIN_RATE_WIN_COUNT_NEGATIVE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_07", "승리 횟수는 음수일 수 없습니다."),
	INVALID_WIN_RATE_WIN_GREATER_THAN_TOTAL(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_08", "승리 횟수가 전체 매매 횟수보다 클 수 없습니다."),
	INVALID_RNR_PNL_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_09", "P&L은 null일 수 없습니다."),
	INVALID_RNR_RISK_TAKING_NULL(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_10", "리스크 테이킹은 null일 수 없습니다."),
	INVALID_RNR_RISK_TAKING_NEGATIVE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_11", "리스크 테이킹은 음수일 수 없습니다."),
	FEEDBACK_PERIOD_MISMATCH(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_12", "피드백 연/월/주차 정보가 요청 날짜와 일치하지 않습니다."),
	INVALID_YEAR_MONTH(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_13", "연도/월은 현재 또는 과거만 가능합니다."),
	INVALID_DATE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_14", "연도/월/일은 현재 또는 과거만 가능합니다."),
	INVALID_LEVERAGE_RANGE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_15", "레버리지는 1 ~ 125 사이의 값이어야 합니다."),
	INVALID_OPERATING_FUNDS_RATIO_RANGE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_16", "비중은 1 ~ 100 사이의 값이어야 합니다."),
	INVALID_LONG_POSITION_PROFIT_PRICE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_17", "롱 포지션에서 수익(PNL > 0)인 경우, 탈출 가격은 진입 가격보다 높아야 합니다."),
	INVALID_LONG_POSITION_LOSS_PRICE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_18", "롱 포지션에서 손실(PNL < 0)인 경우, 탈출 가격은 진입 가격보다 낮아야 합니다."),
	INVALID_SHORT_POSITION_PROFIT_PRICE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_19", "숏 포지션에서 수익(PNL > 0)인 경우, 탈출 가격은 진입 가격보다 낮아야 합니다."),
	INVALID_SHORT_POSITION_LOSS_PRICE(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_20", "숏 포지션에서 손실(PNL < 0)인 경우, 탈출 가격은 진입 가격보다 높아야 합니다."),
	INSUFFICIENT_TOKEN(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_21", "토큰이 부족합니다."),
	TOKEN_REQUIRED_FOR_BASIC_MEMBERSHIP(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_22", "BASIC 멤버십은 토큰을 사용해야 피드백 요청이 가능합니다."),
	TOKEN_NOT_ALLOWED_FOR_PREMIUM_MEMBERSHIP(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_23", "PREMIUM 멤버십은 토큰을 사용할 수 없습니다."),
	COURSE_STATUS_MISMATCH(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_24", "요청한 완강 상태가 현재 사용자의 완강 상태와 일치하지 않습니다."),
	COMPLETED_FEEDBACK_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FEEDBACK_REQ_400_25", "피드백 답변이 완료된 요청은 수정할 수 없습니다."),
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
