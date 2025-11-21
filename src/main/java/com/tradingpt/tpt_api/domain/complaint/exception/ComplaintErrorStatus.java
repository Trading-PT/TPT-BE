package com.tradingpt.tpt_api.domain.complaint.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 민원 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: COMPLAINT_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum ComplaintErrorStatus implements BaseCodeInterface {

    // 404 Not Found
    COMPLAINT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPLAINT_404_0", "민원을 찾을 수 없습니다.");

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
