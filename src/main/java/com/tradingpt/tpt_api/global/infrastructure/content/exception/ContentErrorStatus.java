package com.tradingpt.tpt_api.global.infrastructure.content.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 콘텐츠 처리 인프라 에러 상태 코드 정의
 *
 * 에러 코드 형식: CONTENT_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum ContentErrorStatus implements BaseCodeInterface {

	// 500 Internal Server Error
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONTENT_500_0", "이미지 업로드에 실패했습니다."),
	CONTENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONTENT_500_1", "콘텐츠 처리 중 오류가 발생했습니다."),

	// 400 Bad Request
	INVALID_CONTENT_FORMAT(HttpStatus.BAD_REQUEST, "CONTENT_400_0", "콘텐츠 형식이 올바르지 않습니다."),
	INVALID_BASE64_FORMAT(HttpStatus.BAD_REQUEST, "CONTENT_400_1", "Base64 인코딩이 올바르지 않습니다."),
	UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "CONTENT_400_2", "지원하지 않는 이미지 형식입니다. (JPEG, PNG, GIF, WebP만 지원)"),
	IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "CONTENT_400_3", "이미지 크기가 제한을 초과했습니다. (최대 10MB)"),
	EMPTY_CONTENT(HttpStatus.BAD_REQUEST, "CONTENT_400_4", "처리할 콘텐츠가 비어있습니다."),
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
