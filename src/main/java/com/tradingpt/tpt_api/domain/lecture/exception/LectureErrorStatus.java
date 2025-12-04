package com.tradingpt.tpt_api.domain.lecture.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 강의 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: LECTURE_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum LectureErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	NOT_FOUND(HttpStatus.NOT_FOUND, "LECTURE_404_0", "해당 강의를 찾을 수 없습니다."),
	ASSIGNMENT_NOT_SUBMITTED(HttpStatus.NOT_FOUND, "LECTURE_404_1", "해당 과제를 찾을 수 없습니다."),
	VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "LECTURE_404_2", "해당 강의를 찾을 수 없습니다."),
	ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "LECTURE_404_3", "해당 첨부파일을 찾을 수 없습니다."),
	PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "LECTURE_404_1", "해당 강의를 찾을 수 없습니다."),

	// 400 Bad Request
	DELETE_FAILED(HttpStatus.BAD_REQUEST, "LECTURE_400_0", "강의 삭제 중 오류가 발생했습니다."),
	INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "LECTURE_400_1", "유효하지 않은 강의 공개범위(category) 입니다."),
	ALREADY_FREE_LECTURE(HttpStatus.BAD_REQUEST, "LECTURE_400_2", "유료 강의는 구매할 수 없습니다."),
	ALREADY_PURCHASED(HttpStatus.BAD_REQUEST, "LECTURE_400_3", "이미 구매한 강의입니다."),
	NOT_ENOUGH_TOKENS(HttpStatus.BAD_REQUEST, "LECTURE_400_4", "보유 토큰이 부족합니다."),
	LECTURE_EXPIRED(HttpStatus.FORBIDDEN, "LECTURE_400_5", "수강 기간이 만료된 강의입니다."),
	INVALID_LECTURE_ORDER(HttpStatus.FORBIDDEN, "LECTURE_400_5", "올바른 lecture_order가 아닙니다."),

	INVALID_ATTACHMENT_FOR_LECTURE(HttpStatus.BAD_REQUEST, "LECTURE_400_6", "해당 강의에 속한 첨부파일이 아닙니다.");

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

