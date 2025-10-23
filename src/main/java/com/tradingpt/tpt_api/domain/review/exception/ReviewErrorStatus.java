package com.tradingpt.tpt_api.domain.review.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorStatus implements BaseCodeInterface {

	// 400 BAD REQUEST - 클라이언트의 잘못된 요청
	REVIEW_ALREADY_HAS_REPLY(HttpStatus.BAD_REQUEST, "REVIEW_400_1", "이미 답변이 작성된 리뷰입니다."),
	INVALID_REVIEW_CONTENT(HttpStatus.BAD_REQUEST, "REVIEW_400_2", "리뷰 내용이 올바르지 않습니다."),

	// 403 FORBIDDEN - 권한 없음 (로그인은 했지만 접근 권한 없음)
	UNAUTHORIZED_REVIEW_ACCESS(HttpStatus.FORBIDDEN, "REVIEW_403_1", "해당 리뷰에 접근할 권한이 없습니다."),
	REVIEW_NOT_PUBLIC(HttpStatus.FORBIDDEN, "REVIEW_403_2", "비공개 리뷰는 조회할 수 없습니다."),

	// 404 NOT FOUND - 리소스를 찾을 수 없음
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_404_1", "리뷰를 찾을 수 없습니다."),
	REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_404_2", "답변을 찾을 수 없습니다.");

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