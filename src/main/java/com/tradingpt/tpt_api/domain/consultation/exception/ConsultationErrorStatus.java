package com.tradingpt.tpt_api.domain.consultation.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 상담 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: CONSULT_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum ConsultationErrorStatus implements BaseCodeInterface {

    // 409 Conflict
    BLOCKED(HttpStatus.CONFLICT, "CONSULT_409_0", "해당 시간대는 예약 차단되었습니다."),
    FULL(HttpStatus.CONFLICT, "CONSULT_409_1", "해당 시간대 예약 정원이 가득 찼습니다."),
    DUPLICATE(HttpStatus.CONFLICT, "CONSULT_409_2", "이미 동일 슬롯에 예약이 있습니다."),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, "CONSULT_404_0", "해당 상담을 찾을 수 없습니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "CONSULT_403_0", "해당 상담에 대한 권한이 없습니다."),

    // 400 Bad Request
    INVALID_TIME(HttpStatus.BAD_REQUEST, "CONSULT_400_0", "유효하지 않은 상담 시간입니다."),
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
