package com.tradingpt.tpt_api.domain.paymentmethod.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PaymentMethod 도메인 에러 상태
 */
@Getter
@AllArgsConstructor
public enum PaymentMethodErrorStatus implements BaseCodeInterface {

	// 404 NOT FOUND
	PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "PM001", "결제 수단을 찾을 수 없습니다."),
	BILLING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "PM404_1", "빌링키 요청을 찾을 수 없습니다."),

	// 400 BAD REQUEST
	INVALID_BILLING_KEY(HttpStatus.BAD_REQUEST, "PM002", "유효하지 않은 빌링키입니다."),
	INVALID_AUTH_TOKEN(HttpStatus.BAD_REQUEST, "PM003", "유효하지 않은 인증 토큰입니다."),
	INVALID_MOID(HttpStatus.BAD_REQUEST, "PM004", "유효하지 않은 주문번호입니다."),
	CANNOT_DELETE_ONLY_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "PM005", "마지막 결제 수단은 삭제할 수 없습니다."),
	PAYMENT_METHOD_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PM006", "이미 등록된 결제 수단입니다."),

	// 403 FORBIDDEN
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "PM010", "해당 결제 수단에 접근할 권한이 없습니다."),

	// 500 INTERNAL SERVER ERROR
	BILLING_KEY_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PM100", "빌링키 등록에 실패했습니다."),
	BILLING_KEY_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PM101", "빌링키 삭제에 실패했습니다."),
	PAYMENT_METHOD_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PM102", "결제 수단 저장에 실패했습니다.");

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
