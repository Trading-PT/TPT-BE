package com.tradingpt.tpt_api.domain.payment.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 결제 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: PAYMENT_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum PaymentErrorStatus implements BaseCodeInterface {

	// 502 Bad Gateway
	NICEPAY_API_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT_502_0", "나이스페이 API 오류가 발생했습니다."),

	// 500 Internal Server Error
	NICEPAY_SIGNATURE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_0", "나이스페이 서명 생성에 실패했습니다."),
	NICEPAY_TID_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_1", "나이스페이 거래번호 생성에 실패했습니다."),
	NICEPAY_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_2", "나이스페이 응답 파싱에 실패했습니다."),

	// 404 Not Found
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_0", "결제 내역을 찾을 수 없습니다."),
	PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_1", "결제 수단을 찾을 수 없습니다."),
	BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_2", "빌링키를 찾을 수 없습니다."),

	// 409 Conflict
	PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT_409_0", "이미 처리된 결제입니다."),

	// 400 Bad Request
	PAYMENT_EXECUTION_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_400_0", "결제 실행에 실패했습니다."),
	PAYMENT_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_400_1", "결제 금액이 유효하지 않습니다."),
	PAYMENT_METHOD_INACTIVE(HttpStatus.BAD_REQUEST, "PAYMENT_400_2", "비활성화된 결제 수단입니다."),
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
