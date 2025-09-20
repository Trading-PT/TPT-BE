package com.tradingpt.tpt_api.domain.feedbackresponse.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackResponseErrorStatus implements BaseCodeInterface {

	INVALID_CONTENT_FORMAT(HttpStatus.BAD_REQUEST, "FEEDBACK_RESPONSE4001", "피드백 본문 형식이 올바르지 않습니다."),
	UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "FEEDBACK_RESPONSE4002", "지원하지 않는 이미지 형식입니다."),
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FEEDBACK_RESPONSE5001", "이미지 업로드 중 오류가 발생했습니다.");

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

