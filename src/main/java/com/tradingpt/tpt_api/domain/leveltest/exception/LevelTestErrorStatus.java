package com.tradingpt.tpt_api.domain.leveltest.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레벨 테스트 도메인 에러 상태 코드 정의
 * <p>
 * 에러 코드 형식: LEVELTEST_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum LevelTestErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVELTEST_404_0", "해당 문제를 찾을 수 없습니다."),
	ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVELTEST_404_1", "해당 시도를 찾을 수 없습니다."),
	RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVELTEST_404_2", "해당 응답을 찾을 수 없습니다."),

	// 403 Forbidden
	ATTEMPT_NOT_ALLOWED(HttpStatus.FORBIDDEN, "LEVELTEST_403_0", "레벨테스트는 완강 후에만 재시도할 수 있습니다."),

	// 400 Bad Request
	RESPONSE_NOT_IN_ATTEMPT(HttpStatus.BAD_REQUEST, "LEVELTEST_400_0", "응답이 해당 시도에 포함되어 있지 않습니다."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "LEVELTEST_400_1", "잘못된 요청 형식입니다."),
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
