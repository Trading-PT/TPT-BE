package com.tradingpt.tpt_api.domain.consultation.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ConsultationErrorStatus implements BaseCodeInterface {

    BLOCKED(HttpStatus.CONFLICT, "CONSULTATION4001", "해당 시간대는 예약 차단되었습니다."),
    FULL(HttpStatus.CONFLICT, "CONSULTATION4002", "해당 시간대 예약 정원이 가득 찼습니다."),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "CONSULTATION_4003", "유효하지 않은 상담 시간입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "CONSULTATION_NOT_FOUND", "해당 상담을 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "CONSULTATION_FORBIDDEN", "해당 상담에 대한 권한이 없습니다."),
    DUPLICATE(HttpStatus.CONFLICT, "CONSULTATION_DUPLICATE", "이미 동일 슬롯에 예약이 있습니다.");

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
