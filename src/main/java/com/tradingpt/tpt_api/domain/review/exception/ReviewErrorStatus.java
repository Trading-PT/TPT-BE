package com.tradingpt.tpt_api.domain.review.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 리뷰 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: REVIEW_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum ReviewErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_404_0", "리뷰를 찾을 수 없습니다."),
	REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_404_1", "답변을 찾을 수 없습니다."),

	// 403 Forbidden
	UNAUTHORIZED_REVIEW_ACCESS(HttpStatus.FORBIDDEN, "REVIEW_403_0", "해당 리뷰에 접근할 권한이 없습니다."),
	REVIEW_NOT_PUBLIC(HttpStatus.FORBIDDEN, "REVIEW_403_1", "비공개 리뷰는 조회할 수 없습니다."),

	// 400 Bad Request
	REVIEW_ALREADY_HAS_REPLY(HttpStatus.BAD_REQUEST, "REVIEW_400_0", "이미 답변이 작성된 리뷰입니다."),
	INVALID_REVIEW_CONTENT(HttpStatus.BAD_REQUEST, "REVIEW_400_1", "리뷰 내용이 올바르지 않습니다."),
	REVIEW_HAS_NO_REPLY(HttpStatus.BAD_REQUEST, "REVIEW_400_2", "리뷰에 답변이 작성되어 있지 않습니다."),
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
