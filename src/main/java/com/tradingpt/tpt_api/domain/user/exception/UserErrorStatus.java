package com.tradingpt.tpt_api.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 도메인 에러 상태 코드 정의
 * 사용자 관련 비즈니스 로직에서 발생할 수 있는 모든 에러 상태를 정의
 */
@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseCodeInterface {

	// 사용자 4000번대 에러
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4001", "사용자를 찾을 수 없습니다."),
	CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4002", "고객을 찾을 수 없습니다."),
	TRAINER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4003", "트레이너를 찾을 수 없습니다."),
	INVALID_USER_TYPE(HttpStatus.BAD_REQUEST, "USER4004", "잘못된 사용자 타입입니다."),
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER4005", "이미 존재하는 사용자입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "USER4006", "접근 권한이 없습니다."),
	INVALID_INVESTMENT_HISTORY_REQUEST(HttpStatus.BAD_REQUEST, "USER4007", "투자 유형 변경 요청이 올바르지 않습니다."),
	INVALID_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "USER4008", "잘못된 상태 요청입니다."),
	COURSE_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4009", "유저의 완강 여부가 존재하지 않습니다."),
	PASSWORD_CONFIRM_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER4010", "비밀번호가 일치하지 않습니다"),
	LOGIN_ID_DUPLICATED(HttpStatus.BAD_REQUEST, "USER4011", "이미 존재하는 id입니다");

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
