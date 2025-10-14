package com.tradingpt.tpt_api.global.infrastructure.content.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 콘텐츠 처리 관련 에러 상태 코드
 */
@Getter
@AllArgsConstructor
public enum ContentErrorStatus implements BaseCodeInterface {

	// 4000번대 - 클라이언트 에러
	INVALID_CONTENT_FORMAT(HttpStatus.BAD_REQUEST, "CONTENT_4001", "콘텐츠 형식이 올바르지 않습니다."),
	INVALID_BASE64_FORMAT(HttpStatus.BAD_REQUEST, "CONTENT_4002", "Base64 인코딩이 올바르지 않습니다."),
	UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "CONTENT_4003", "지원하지 않는 이미지 형식입니다. (JPEG, PNG, GIF, WebP만 지원)"),
	IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "CONTENT_4004", "이미지 크기가 제한을 초과했습니다. (최대 10MB)"),
	EMPTY_CONTENT(HttpStatus.BAD_REQUEST, "CONTENT_4005", "처리할 콘텐츠가 비어있습니다."),

	// 5000번대 - 서버 에러
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONTENT_5001", "이미지 업로드에 실패했습니다."),
	CONTENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONTENT_5002", "콘텐츠 처리 중 오류가 발생했습니다.");

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