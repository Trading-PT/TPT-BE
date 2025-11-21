package com.tradingpt.tpt_api.domain.feedbackresponse.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 피드백 답변 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: FEEDBACK_RES_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum FeedbackResponseErrorStatus implements BaseCodeInterface {

	// 500 Internal Server Error
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FEEDBACK_RES_500_0", "이미지 업로드 중 오류가 발생했습니다."),

	// 400 Bad Request
	INVALID_CONTENT_FORMAT(HttpStatus.BAD_REQUEST, "FEEDBACK_RES_400_0", "피드백 본문 형식이 올바르지 않습니다."),
	UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "FEEDBACK_RES_400_1", "지원하지 않는 이미지 형식입니다."),
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
