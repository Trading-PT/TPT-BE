package com.tradingpt.tpt_api.domain.auth.exception.code;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 인증/인가 관련 전용 에러 상태 코드
 * Trading PT API의 인증 시스템에서 발생할 수 있는 모든 에러 상태를 정의
 */
@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseCodeInterface {

	// ========== JWT 토큰 관련 에러 ==========
	JWT_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4001", "JWT 토큰 인증에 실패했습니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4002", "토큰이 만료되었습니다."),
	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4003", "유효하지 않은 토큰입니다."),
	TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH4004", "토큰 형식이 올바르지 않습니다."),
	TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "AUTH4005", "지원하지 않는 토큰입니다."),
	TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4006", "토큰 서명이 유효하지 않습니다."),

	// ========== 자격 증명 관련 에러 ==========
	BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4010", "아이디 또는 비밀번호가 올바르지 않습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH4011", "사용자를 찾을 수 없습니다."),
	AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4012", "인증에 실패했습니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH4013", "접근 권한이 없습니다."),
	ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "AUTH4014", "비활성화된 계정입니다."),
	ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "AUTH4015", "잠긴 계정입니다."),
	CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4016", "비밀번호 유효 기간이 만료되었습니다."),
	ACCOUNT_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4017", "계정 유효 기간이 만료되었습니다."),

	// ========== 회원가입 관련 에러 ==========
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH4020", "이미 존재하는 이메일입니다."),
	EMAIL_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "AUTH4021", "올바르지 않은 이메일 형식입니다."),
	PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "AUTH4022", "비밀번호가 너무 약합니다."),
	PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "AUTH4023", "비밀번호는 최소 8자 이상이어야 합니다."),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH4024", "비밀번호가 일치하지 않습니다."),
	USERNAME_TOO_SHORT(HttpStatus.BAD_REQUEST, "AUTH4025", "사용자명은 최소 2자 이상이어야 합니다."),
	USERNAME_TOO_LONG(HttpStatus.BAD_REQUEST, "AUTH4026", "사용자명은 최대 30자까지 가능합니다."),
	USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH4027", "이미 존재하는 사용자명입니다."),

	// ========== OAuth2 소셜 로그인 관련 에러 ==========
	OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4030", "소셜 로그인 인증에 실패했습니다. 다시 시도해 주세요."),
	OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AUTH4031", "지원하지 않는 소셜 로그인 제공자입니다."),
	OAUTH2_USER_INFO_RETRIEVAL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH4032", "소셜 로그인 사용자 정보를 가져오는데 실패했습니다."),
	OAUTH2_CALLBACK_ERROR(HttpStatus.BAD_REQUEST, "AUTH4033", "소셜 로그인 콜백 처리 중 오류가 발생했습니다."),
	OAUTH2_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH4034", "OAuth2 state 파라미터가 일치하지 않습니다."),

	// ========== 토큰 관리 관련 에러 ==========
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH4040", "Refresh Token을 찾을 수 없습니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4041", "Refresh Token이 만료되었습니다."),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4042", "유효하지 않은 Refresh Token입니다."),
	ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH4043", "Access Token이 필요합니다."),
	BEARER_TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH4044", "Bearer 토큰 형식이 올바르지 않습니다."),
	TOKEN_VERSION_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH4045", "토큰 버전이 일치하지 않습니다."),

	// ========== 권한 관련 에러 ==========
	INSUFFICIENT_PRIVILEGES(HttpStatus.FORBIDDEN, "AUTH4050", "권한이 부족합니다."),
	ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "AUTH4051", "관리자 권한이 필요합니다."),
	USER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "AUTH4052", "사용자 권한이 필요합니다."),
	PREMIUM_REQUIRED(HttpStatus.FORBIDDEN, "AUTH4053", "프리미엄 권한이 필요합니다."),

	// ========== Security Handler 전용 에러 ==========
	AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH4054", "인증이 필요합니다. 로그인 후 다시 시도해주세요."),
	AUTHENTICATION_REQUIRED_ADMIN(HttpStatus.UNAUTHORIZED, "AUTH4055",
		"인증이 필요합니다. 로그인 후 ROLE_ADMIN 또는 ROLE_TRAINER 권한으로 접근해주세요."),
	ACCESS_DENIED_GENERAL(HttpStatus.FORBIDDEN, "AUTH4056", "접근 권한이 없습니다. 필요한 권한을 확인해주세요."),
	ACCESS_DENIED_ADMIN(HttpStatus.FORBIDDEN, "AUTH4057", "접근 권한이 없습니다. ROLE_ADMIN 또는 ROLE_TRAINER 권한이 필요합니다."),

	// ========== 계정 상태 관련 에러 ==========
	ACCOUNT_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH4060", "이메일 인증이 필요합니다."),
	ACCOUNT_SUSPENDED(HttpStatus.UNAUTHORIZED, "AUTH4061", "정지된 계정입니다."),
	ACCOUNT_DELETED(HttpStatus.UNAUTHORIZED, "AUTH4062", "삭제된 계정입니다."),
	PHONE_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH4063", "전화번호 인증이 필요합니다."),

	// ========== 보안 관련 에러 ==========
	TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH4070", "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),
	SUSPICIOUS_ACTIVITY_DETECTED(HttpStatus.UNAUTHORIZED, "AUTH4071", "의심스러운 활동이 감지되었습니다."),
	IP_BLOCKED(HttpStatus.FORBIDDEN, "AUTH4072", "차단된 IP에서의 접근입니다."),
	DEVICE_NOT_TRUSTED(HttpStatus.FORBIDDEN, "AUTH4073", "신뢰할 수 없는 디바이스입니다."),
	SECURITY_VIOLATION(HttpStatus.FORBIDDEN, "AUTH4074", "보안 위반이 감지되어 모든 세션이 무효화되었습니다."),

	// ========== 입력 검증 관련 에러 ==========
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH4080", "입력 데이터 검증에 실패했습니다."),
	REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "AUTH4081", "필수 필드가 누락되었습니다."),
	INVALID_INPUT_FORMAT(HttpStatus.BAD_REQUEST, "AUTH4082", "입력 형식이 올바르지 않습니다."),
	PASSWORD_CONFIRMATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH4083", "비밀번호 확인이 일치하지 않습니다."),
	PASSWORD_REUSED(HttpStatus.BAD_REQUEST, "AUTH4084", "현재 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),

	// ========== 트레이딩 특화 인증 에러 ==========
	TRADING_ACCOUNT_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH4090", "트레이딩 계정 인증이 필요합니다."),
	KYC_VERIFICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH4091", "본인인증(KYC)이 필요합니다."),
	RISK_ASSESSMENT_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH4092", "투자성향 평가가 필요합니다."),
	TERMS_AGREEMENT_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH4093", "약관 동의가 필요합니다."),
	AGE_VERIFICATION_FAILED(HttpStatus.FORBIDDEN, "AUTH4094", "만 18세 이상만 이용 가능합니다."),

	// ========== 세션 관리 에러 ==========
	SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4100", "세션이 만료되었습니다."),
	SESSION_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4101", "유효하지 않은 세션입니다."),
	CONCURRENT_SESSION_DETECTED(HttpStatus.CONFLICT, "AUTH4102", "다른 곳에서 로그인이 감지되었습니다."),
	SESSION_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "AUTH4103", "최대 세션 수를 초과했습니다."),

	// ========== 이메일/SMS 인증 관련 에러 ==========
	VERIFICATION_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4110", "인증 코드가 만료되었습니다."),
	VERIFICATION_CODE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4111", "인증 코드가 올바르지 않습니다."),
	VERIFICATION_CODE_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH4112", "인증 시도 횟수를 초과했습니다."),
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH4113", "이메일 발송에 실패했습니다."),
	SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH4114", "SMS 발송에 실패했습니다."),

	// ========== CSRF 인증 관련 에러 ==========
	CSRF_TOKEN_INVALID(HttpStatus.FORBIDDEN, "AUTH4120",
		"CSRF 토큰이 유효하지 않습니다. 페이지를 새로고침 후 다시 시도해주세요."),
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
