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

    // ========== 일반적인 HTTP 에러 ==========
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON4000", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON4001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON4003", "접근이 거부되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON4004", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON4005", "허용되지 않은 HTTP 메소드입니다."),
    CONFLICT(HttpStatus.CONFLICT, "COMMON4009", "리소스 충돌이 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5000", "서버 내부 오류가 발생했습니다."),

    // ========== 유효성 검사 에러 ==========
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALID4000", "입력 데이터 검증에 실패했습니다."),
    MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "VALID4001", "필수 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_FORMAT(HttpStatus.BAD_REQUEST, "VALID4002", "파라미터 형식이 잘못되었습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "VALID4003", "이메일 형식이 올바르지 않습니다."),
    INVALID_PHONE_FORMAT(HttpStatus.BAD_REQUEST, "VALID4004", "전화번호 형식이 올바르지 않습니다."),

    // ========== 인증/인가 관련 에러 ==========
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4002", "만료된 토큰입니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4003", "토큰이 제공되지 않았습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4004", "잘못된 인증 정보입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4005", "로그인에 실패했습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH4006", "비밀번호가 일치하지 않습니다."),
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "AUTH4030", "권한이 부족합니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AUTH4031", "계정이 잠겨있습니다."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "AUTH4032", "비활성화된 계정입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH4033", "이메일 인증이 필요합니다."),

    // ========== 사용자 관련 에러 ==========
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4040", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER4090", "이미 존재하는 사용자입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER4091", "이미 사용중인 이메일입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER4092", "이미 사용중인 사용자명입니다."),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER4093", "이미 사용중인 전화번호입니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "USER4000", "비밀번호가 보안 정책에 맞지 않습니다."),

    // ========== 트레이딩/금융 관련 에러 ==========
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "TRADE4000", "잔액이 부족합니다."),
    INVALID_TRADE_AMOUNT(HttpStatus.BAD_REQUEST, "TRADE4001", "거래 금액이 유효하지 않습니다."),
    MARKET_CLOSED(HttpStatus.BAD_REQUEST, "TRADE4002", "시장이 닫혀있습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE4040", "주문을 찾을 수 없습니다."),
    ORDER_ALREADY_EXECUTED(HttpStatus.CONFLICT, "TRADE4090", "이미 실행된 주문입니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.CONFLICT, "TRADE4091", "이미 취소된 주문입니다."),
    INVALID_SYMBOL(HttpStatus.BAD_REQUEST, "TRADE4003", "유효하지 않은 심볼입니다."),
    PRICE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "TRADE4004", "가격이 허용 범위를 벗어났습니다."),
    TRADING_SUSPENDED(HttpStatus.BAD_REQUEST, "TRADE4005", "거래가 일시 중단되었습니다."),

    // ========== 포트폴리오 관련 에러 ==========
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "PORT4040", "포트폴리오를 찾을 수 없습니다."),
    PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORT4030", "포트폴리오 접근이 거부되었습니다."),
    INVALID_PORTFOLIO_DATA(HttpStatus.BAD_REQUEST, "PORT4000", "포트폴리오 데이터가 유효하지 않습니다."),

    // ========== 데이터베이스 관련 에러 ==========
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB5000", "데이터베이스 오류가 발생했습니다."),
    DATA_INTEGRITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB5001", "데이터 무결성 오류가 발생했습니다."),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "DB4000", "데이터 제약 조건을 위반했습니다."),

    // ========== 외부 API 관련 에러 ==========
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EXT5000", "외부 API 호출 중 오류가 발생했습니다."),
    EXTERNAL_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "EXT5004", "외부 API 요청이 시간 초과되었습니다."),
    EXTERNAL_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "EXT5003", "외부 API 서비스를 사용할 수 없습니다."),

    // ========== 파일 관련 에러 ==========
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE4040", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "FILE4000", "파일 업로드에 실패했습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "FILE4001", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE4002", "파일 크기가 제한을 초과했습니다."),

    // ========== 비즈니스 로직 에러 ==========
    BUSINESS_LOGIC_ERROR(HttpStatus.BAD_REQUEST, "BIZ4000", "비즈니스 규칙 위반입니다."),
    OPERATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "BIZ4030", "현재 상태에서는 해당 작업을 수행할 수 없습니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "BIZ4290", "요청 한도를 초과했습니다.");

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
