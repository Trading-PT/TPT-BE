package com.tradingpt.tpt_api.domain.feedbackrequest.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 피드백 요청 도메인 에러 상태 코드 정의
 * 피드백 요청 관련 비즈니스 로직에서 발생할 수 있는 모든 에러 상태를 정의
 */
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
	REQUEST_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "FEEDBACK4008", "요청 날짜가 필수입니다.");

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
