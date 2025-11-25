package com.tradingpt.tpt_api.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: USER_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseCodeInterface {

	// 500 Internal Server Error
	TRAINER_NOT_ASSIGNED(HttpStatus.INTERNAL_SERVER_ERROR, "USER_500_0", "배정된 트레이너가 없습니다."),

	// 404 Not Found
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_0", "사용자를 찾을 수 없습니다."),
	CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "고객을 찾을 수 없습니다."),
	TRAINER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_2", "트레이너를 찾을 수 없습니다."),
	ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_3", "어드민을 찾을 수 없습니다."),
	COURSE_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_4", "유저의 완강 여부가 존재하지 않습니다."),
	UID_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_5", "uid를 찾을 수 없습니다."),

	// 403 Forbidden
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "USER_403_0", "접근 권한이 없습니다."),
	NOT_TRAINERS_CUSTOMER(HttpStatus.FORBIDDEN, "USER_403_1", "나에게 배정된 고객이 아닙니다."),

	// 409 Conflict
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_0", "이미 존재하는 사용자입니다."),

	// 400 Bad Request
	INVALID_USER_TYPE(HttpStatus.BAD_REQUEST, "USER_400_0", "잘못된 사용자 타입입니다."),
	INVALID_INVESTMENT_HISTORY_REQUEST(HttpStatus.BAD_REQUEST, "USER_400_1", "투자 유형 변경 요청이 올바르지 않습니다."),
	INVALID_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "USER_400_2", "잘못된 상태 요청입니다."),
	PASSWORD_CONFIRM_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER_400_3", "비밀번호가 일치하지 않습니다"),
	LOGIN_ID_DUPLICATED(HttpStatus.BAD_REQUEST, "USER_400_4", "이미 존재하는 id입니다"),
	HAS_ASSIGNED_CUSTOMERS(HttpStatus.BAD_REQUEST, "USER_400_5", "해당 트레이너에게 배정된 고객이 존재합니다."),
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
