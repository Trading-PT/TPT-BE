package com.tradingpt.tpt_api.domain.payment.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 결제 도메인 에러 상태 코드 정의
 * 결제 관련 비즈니스 로직에서 발생할 수 있는 모든 에러 상태를 정의
 */
@Getter
@AllArgsConstructor
public enum PaymentErrorStatus implements BaseCodeInterface {

    // 결제 8000번대 에러
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT8001", "결제 내역을 찾을 수 없습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT8002", "이미 처리된 결제입니다."),
    PAYMENT_EXECUTION_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT8003", "결제 실행에 실패했습니다."),
    PAYMENT_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT8004", "결제 금액이 유효하지 않습니다."),
    PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT8005", "결제 수단을 찾을 수 없습니다."),
    PAYMENT_METHOD_INACTIVE(HttpStatus.BAD_REQUEST, "PAYMENT8006", "비활성화된 결제 수단입니다."),
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT8007", "빌링키를 찾을 수 없습니다."),

    // 나이스페이 연동 에러
    NICEPAY_API_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT8101", "나이스페이 API 오류가 발생했습니다."),
    NICEPAY_SIGNATURE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT8102", "나이스페이 서명 생성에 실패했습니다."),
    NICEPAY_TID_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT8103", "나이스페이 거래번호 생성에 실패했습니다."),
    NICEPAY_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT8104", "나이스페이 응답 파싱에 실패했습니다."),
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
