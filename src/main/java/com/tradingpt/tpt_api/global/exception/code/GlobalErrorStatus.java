package com.tradingpt.tpt_api.global.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전역 에러 상태 코드 정의
 * Trading PT API에서 발생할 수 있는 모든 에러 상태를 정의
 */
@Getter
@AllArgsConstructor
public enum GlobalErrorStatus implements BaseCodeInterface {

	// 가장 일반적인 응답
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
	_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
	_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

	// Common 4000번대 에러
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON4001", "잘못된 파라미터입니다."),
	INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "COMMON4002", "잘못된 파라미터 타입입니다."),
	MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON4003", "필수 파라미터가 누락되었습니다."),
	INVALID_FORMAT(HttpStatus.BAD_REQUEST, "COMMON4004", "잘못된 형식입니다."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON4005", "입력값 검증에 실패했습니다."),
	INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON4006", "요청 본문이 유효하지 않습니다."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON4007", "요청한 리소스를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON4008", "허용되지 않은 HTTP 메소드입니다."),
	NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "COMMON4009", "허용되지 않는 요청입니다."),
	CONFLICT(HttpStatus.CONFLICT, "COMMON4010", "요청이 현재 서버 상태와 충돌합니다."),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON4011", "지원하지 않는 미디어 타입입니다."),

	// Common 5000번대 에러
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5001", "데이터베이스 오류가 발생했습니다."),
	EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5002", "외부 API 호출 중 오류가 발생했습니다."),
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMMON5003", "서비스를 일시적으로 사용할 수 없습니다.");

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
