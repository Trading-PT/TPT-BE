package com.tradingpt.tpt_api.global.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전역 에러 상태 코드 정의
 * Trading PT API에서 발생할 수 있는 모든 에러 상태를 정의
 *
 * 에러 코드 형식: GLOBAL_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum GlobalErrorStatus implements BaseCodeInterface {

	// 500 Internal Server Error
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_500_0", "서버 에러, 관리자에게 문의 바랍니다."),
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_500_1", "데이터베이스 오류가 발생했습니다."),
	EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_500_2", "외부 API 호출 중 오류가 발생했습니다."),

	// 503 Service Unavailable
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "GLOBAL_503_0", "서비스를 일시적으로 사용할 수 없습니다."),

	// 415 Unsupported Media Type
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "GLOBAL_415_0", "지원하지 않는 미디어 타입입니다."),

	// 409 Conflict
	CONFLICT(HttpStatus.CONFLICT, "GLOBAL_409_0", "요청이 현재 서버 상태와 충돌합니다."),

	// 406 Not Acceptable
	NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "GLOBAL_406_0", "허용되지 않는 요청입니다."),

	// 405 Method Not Allowed
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_405_0", "허용되지 않은 HTTP 메소드입니다."),

	// 404 Not Found
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "GLOBAL_404_0", "요청한 리소스를 찾을 수 없습니다."),

	// 403 Forbidden
	_FORBIDDEN(HttpStatus.FORBIDDEN, "GLOBAL_403_0", "금지된 요청입니다."),

	// 401 Unauthorized
	_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "GLOBAL_401_0", "인증이 필요합니다."),

	// 400 Bad Request
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_400_0", "잘못된 요청입니다."),
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL_400_1", "잘못된 파라미터입니다."),
	INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "GLOBAL_400_2", "잘못된 파라미터 타입입니다."),
	MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL_400_3", "필수 파라미터가 누락되었습니다."),
	INVALID_FORMAT(HttpStatus.BAD_REQUEST, "GLOBAL_400_4", "잘못된 형식입니다."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GLOBAL_400_5", "입력값 검증에 실패했습니다."),
	INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "GLOBAL_400_6", "요청 본문이 유효하지 않습니다."),
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
