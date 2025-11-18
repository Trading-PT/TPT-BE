package com.tradingpt.tpt_api.global.infrastructure.nicepay.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * NicePay API 에러 상태 코드
 */
@Getter
@AllArgsConstructor
public enum NicePayErrorStatus implements BaseCodeInterface {

    // 빌키 발급 관련
    BILLING_KEY_REGISTRATION_FAILED(HttpStatus.BAD_REQUEST, "F200", "빌키 발급에 실패했습니다."),
    INVALID_AUTHENTICATION_TOKEN(HttpStatus.BAD_REQUEST, "F201", "유효하지 않은 인증 토큰입니다."),
    INVALID_TID(HttpStatus.BAD_REQUEST, "F202", "유효하지 않은 거래 ID입니다."),
    CARD_REGISTRATION_FAILED(HttpStatus.BAD_REQUEST, "F203", "카드 등록에 실패했습니다."),

    // 카드 정보 검증 관련
    INVALID_CARD_NUMBER(HttpStatus.BAD_REQUEST, "F110", "유효하지 않은 카드번호입니다. 카드번호를 확인해주세요."),
    INVALID_EXPIRY_DATE(HttpStatus.BAD_REQUEST, "F111", "카드 유효기간이 올바르지 않습니다. 유효기간을 확인해주세요."),
    INVALID_CVC(HttpStatus.BAD_REQUEST, "F112", "CVC 번호가 올바르지 않습니다. CVC를 확인해주세요."),
    PASSWORD_RETRY_EXCEEDED(HttpStatus.BAD_REQUEST, "F113", "카드 비밀번호 입력 횟수를 초과했습니다. 다른 카드로 시도하거나 잠시 후 다시 시도해주세요."),
    INVALID_BIRTH_OR_BIZ_NUMBER(HttpStatus.BAD_REQUEST, "F114", "생년월일 또는 사업자번호가 올바르지 않습니다. 정보를 확인해주세요."),

    // 빌키 삭제 관련
    BILLING_KEY_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F300", "빌키 삭제에 실패했습니다."),
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "F301", "존재하지 않는 빌키입니다."),

    // 시스템 에러
    API_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "NicePay API 연결에 실패했습니다."),
    API_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "NicePay API 요청 시간이 초과되었습니다."),
    INVALID_RESPONSE_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "NicePay API 응답 형식이 올바르지 않습니다."),
    SIGNATURE_VERIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S004", "서명 검증에 실패했습니다."),
    ENCODING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S005", "인코딩 처리 중 오류가 발생했습니다."),

    // 기타
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "9999", "알 수 없는 오류가 발생했습니다.");

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

    /**
     * NicePay API ResultCode로부터 ErrorStatus를 찾습니다.
     * F100(성공)이 아닌 경우 적절한 에러 상태를 반환합니다.
     *
     * @param resultCode NicePay API ResultCode
     * @return 해당하는 NicePayErrorStatus
     */
    public static NicePayErrorStatus fromResultCode(String resultCode) {
        if (resultCode == null || resultCode.trim().isEmpty()) {
            return INVALID_RESPONSE_FORMAT;
        }

        // F100은 정상 응답이므로 이 메서드가 호출되면 안됨
        if ("F100".equals(resultCode)) {
            return UNKNOWN_ERROR;
        }

        // 특정 에러 코드 처리
        switch (resultCode) {
            case "F110":
                return INVALID_CARD_NUMBER;
            case "F111":
                return INVALID_EXPIRY_DATE;
            case "F112":
                return INVALID_CVC;
            case "F113":
                return PASSWORD_RETRY_EXCEEDED;
            case "F114":
                return INVALID_BIRTH_OR_BIZ_NUMBER;
            default:
                break;
        }

        // 빌키 발급 관련 에러 (F2xx)
        if (resultCode.startsWith("F2")) {
            return BILLING_KEY_REGISTRATION_FAILED;
        }

        // 빌키 삭제 관련 에러 (F3xx)
        if (resultCode.startsWith("F3")) {
            return BILLING_KEY_DELETION_FAILED;
        }

        return UNKNOWN_ERROR;
    }
}
