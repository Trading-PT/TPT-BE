package com.tradingpt.tpt_api.domain.auth.exception.code;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 인증/인가 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: AUTH_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 401, 403, 404, 409, 429, 500)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9, 2자리 확장 00-99)
 */
@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseCodeInterface {

	// 500 Internal Server Error
	OAUTH2_USER_INFO_RETRIEVAL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500_0", "소셜 로그인 사용자 정보를 가져오는데 실패했습니다."),
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500_1", "이메일 발송에 실패했습니다."),
	SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500_2", "SMS 발송에 실패했습니다."),

	// 429 Too Many Requests
	TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH_429_0", "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),
	VERIFICATION_CODE_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_429_1", "인증 시도 횟수를 초과했습니다."),

	// 409 Conflict
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_409_0", "이미 존재하는 이메일입니다."),
	USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_409_1", "이미 존재하는 사용자명입니다."),
	CONCURRENT_SESSION_DETECTED(HttpStatus.CONFLICT, "AUTH_409_2", "다른 곳에서 로그인이 감지되었습니다."),
	SESSION_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "AUTH_409_3", "최대 세션 수를 초과했습니다."),

	// 404 Not Found
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_404_0", "사용자를 찾을 수 없습니다."),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_404_1", "Refresh Token을 찾을 수 없습니다."),

	// 403 Forbidden
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403_00", "접근 권한이 없습니다."),
	INSUFFICIENT_PRIVILEGES(HttpStatus.FORBIDDEN, "AUTH_403_01", "권한이 부족합니다."),
	ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "AUTH_403_02", "관리자 권한이 필요합니다."),
	USER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "AUTH_403_03", "사용자 권한이 필요합니다."),
	PREMIUM_REQUIRED(HttpStatus.FORBIDDEN, "AUTH_403_04", "프리미엄 권한이 필요합니다."),
	ACCESS_DENIED_GENERAL(HttpStatus.FORBIDDEN, "AUTH_403_05", "접근 권한이 없습니다. 필요한 권한을 확인해주세요."),
	ACCESS_DENIED_ADMIN(HttpStatus.FORBIDDEN, "AUTH_403_06", "접근 권한이 없습니다. ROLE_ADMIN 또는 ROLE_TRAINER 권한이 필요합니다."),
	IP_BLOCKED(HttpStatus.FORBIDDEN, "AUTH_403_07", "차단된 IP에서의 접근입니다."),
	DEVICE_NOT_TRUSTED(HttpStatus.FORBIDDEN, "AUTH_403_08", "신뢰할 수 없는 디바이스입니다."),
	SECURITY_VIOLATION(HttpStatus.FORBIDDEN, "AUTH_403_09", "보안 위반이 감지되어 모든 세션이 무효화되었습니다."),
	AGE_VERIFICATION_FAILED(HttpStatus.FORBIDDEN, "AUTH_403_10", "만 18세 이상만 이용 가능합니다."),
	CSRF_TOKEN_INVALID(HttpStatus.FORBIDDEN, "AUTH_403_11", "CSRF 토큰이 유효하지 않습니다. 페이지를 새로고침 후 다시 시도해주세요."),

	// 401 Unauthorized
	JWT_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_401_00", "JWT 토큰 인증에 실패했습니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_01", "토큰이 만료되었습니다."),
	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_02", "유효하지 않은 토큰입니다."),
	TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH_401_03", "토큰 형식이 올바르지 않습니다."),
	TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "AUTH_401_04", "지원하지 않는 토큰입니다."),
	TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_05", "토큰 서명이 유효하지 않습니다."),
	BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_06", "아이디 또는 비밀번호가 올바르지 않습니다."),
	AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_401_07", "인증에 실패했습니다."),
	ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "AUTH_401_08", "비활성화된 계정입니다."),
	ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "AUTH_401_09", "잠긴 계정입니다."),
	CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_10", "비밀번호 유효 기간이 만료되었습니다."),
	ACCOUNT_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_11", "계정 유효 기간이 만료되었습니다."),
	OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_401_12", "소셜 로그인 인증에 실패했습니다. 다시 시도해 주세요."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_13", "Refresh Token이 만료되었습니다."),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_14", "유효하지 않은 Refresh Token입니다."),
	ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_15", "Access Token이 필요합니다."),
	BEARER_TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH_401_16", "Bearer 토큰 형식이 올바르지 않습니다."),
	TOKEN_VERSION_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_401_17", "토큰 버전이 일치하지 않습니다."),
	AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_18", "인증이 필요합니다. 로그인 후 다시 시도해주세요."),
	AUTHENTICATION_REQUIRED_ADMIN(HttpStatus.UNAUTHORIZED, "AUTH_401_19", "인증이 필요합니다. 로그인 후 ROLE_ADMIN 또는 ROLE_TRAINER 권한으로 접근해주세요."),
	ACCOUNT_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH_401_20", "이메일 인증이 필요합니다."),
	ACCOUNT_SUSPENDED(HttpStatus.UNAUTHORIZED, "AUTH_401_21", "정지된 계정입니다."),
	ACCOUNT_DELETED(HttpStatus.UNAUTHORIZED, "AUTH_401_22", "삭제된 계정입니다."),
	PHONE_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH_401_23", "전화번호 인증이 필요합니다."),
	SUSPICIOUS_ACTIVITY_DETECTED(HttpStatus.UNAUTHORIZED, "AUTH_401_24", "의심스러운 활동이 감지되었습니다."),
	TRADING_ACCOUNT_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH_401_25", "트레이딩 계정 인증이 필요합니다."),
	KYC_VERIFICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_26", "본인인증(KYC)이 필요합니다."),
	RISK_ASSESSMENT_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_27", "투자성향 평가가 필요합니다."),
	TERMS_AGREEMENT_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_28", "약관 동의가 필요합니다."),
	SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_29", "세션이 만료되었습니다."),
	SESSION_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_30", "유효하지 않은 세션입니다."),
	VERIFICATION_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_31", "인증 코드가 만료되었습니다."),
	VERIFICATION_CODE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_401_32", "인증 코드가 올바르지 않습니다."),

	// 400 Bad Request
	EMAIL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_400_00", "올바르지 않은 이메일 형식입니다."),
	PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "AUTH_400_01", "비밀번호가 너무 약합니다."),
	PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "AUTH_400_02", "비밀번호는 최소 8자 이상이어야 합니다."),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_400_03", "비밀번호가 일치하지 않습니다."),
	USERNAME_TOO_SHORT(HttpStatus.BAD_REQUEST, "AUTH_400_04", "사용자명은 최소 2자 이상이어야 합니다."),
	USERNAME_TOO_LONG(HttpStatus.BAD_REQUEST, "AUTH_400_05", "사용자명은 최대 30자까지 가능합니다."),
	OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AUTH_400_06", "지원하지 않는 소셜 로그인 제공자입니다."),
	OAUTH2_CALLBACK_ERROR(HttpStatus.BAD_REQUEST, "AUTH_400_07", "소셜 로그인 콜백 처리 중 오류가 발생했습니다."),
	OAUTH2_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_400_08", "OAuth2 state 파라미터가 일치하지 않습니다."),
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH_400_09", "입력 데이터 검증에 실패했습니다."),
	REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "AUTH_400_10", "필수 필드가 누락되었습니다."),
	INVALID_INPUT_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_400_11", "입력 형식이 올바르지 않습니다."),
	PASSWORD_CONFIRMATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH_400_12", "비밀번호 확인이 일치하지 않습니다."),
	PASSWORD_REUSED(HttpStatus.BAD_REQUEST, "AUTH_400_13", "현재 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
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
