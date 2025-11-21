package com.tradingpt.tpt_api.domain.paymentmethod.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 결제 수단 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: PAYMENT_MTD_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum PaymentMethodErrorStatus implements BaseCodeInterface {

	// 500 Internal Server Error
	BILLING_KEY_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_MTD_500_0", "빌링키 등록에 실패했습니다."),
	BILLING_KEY_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_MTD_500_1", "빌링키 삭제에 실패했습니다."),
	PAYMENT_METHOD_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_MTD_500_2", "결제 수단 저장에 실패했습니다."),

	// 404 Not Found
	PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_MTD_404_0", "결제 수단을 찾을 수 없습니다."),
	BILLING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_MTD_404_1", "빌링키 요청을 찾을 수 없습니다."),

	// 403 Forbidden
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "PAYMENT_MTD_403_0", "해당 결제 수단에 접근할 권한이 없습니다."),

	// 400 Bad Request
	INVALID_BILLING_KEY(HttpStatus.BAD_REQUEST, "PAYMENT_MTD_400_0", "유효하지 않은 빌링키입니다."),
	INVALID_AUTH_TOKEN(HttpStatus.BAD_REQUEST, "PAYMENT_MTD_400_1", "유효하지 않은 인증 토큰입니다."),
	INVALID_MOID(HttpStatus.BAD_REQUEST, "PAYMENT_MTD_400_2", "유효하지 않은 주문번호입니다."),
	CANNOT_DELETE_ONLY_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "PAYMENT_MTD_400_3", "마지막 결제 수단은 삭제할 수 없습니다."),
	PAYMENT_METHOD_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PAYMENT_MTD_400_4", "이미 등록된 결제수단이 있습니다."),
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
