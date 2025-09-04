package com.tradingpt.tpt_api.auth.util;

import com.tradingpt.tpt_api.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.global.exception.AuthException;

public final class AuthUtil {

    private AuthUtil() {}

    /**
     * 국내 010, 011, 016~019 형태만 허용.
     * 입력이 null/blank면 REQUIRED_FIELD_MISSING,
     * 형식 오류면 INVALID_INPUT_FORMAT 로 예외 발생.
     */
    public static String normalizePhone(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new AuthException(AuthErrorStatus.REQUIRED_FIELD_MISSING, "전화번호를 입력해주세요.");
        }

        String onlyDigits = raw.replaceAll("\\D", ""); // 숫자만 추출

        // 국제번호(82) → 국내 형태로 변환 (예: 821012345678 -> 01012345678)
        if (onlyDigits.startsWith("82")) {
            onlyDigits = "0" + onlyDigits.substring(2);
        }

        // 010/011/016~019 로 시작 & 총 10~11자리
        if (!onlyDigits.matches("01[016789]\\d{7,8}")) {
            throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT, "전화번호 형식이 올바르지 않습니다.");
        }

        return onlyDigits;
    }

    /**
     * 이메일을 trim + lower-case 후 간단한 정규식 검증.
     * 입력이 null/blank면 REQUIRED_FIELD_MISSING,
     * 형식 오류면 EMAIL_INVALID_FORMAT 로 예외 발생.
     */
    public static String normalizeEmail(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new AuthException(AuthErrorStatus.REQUIRED_FIELD_MISSING, "이메일을 입력해주세요.");
        }

        String email = raw.trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new AuthException(AuthErrorStatus.EMAIL_INVALID_FORMAT, "이메일 형식이 올바르지 않습니다.");
        }

        return email;
    }
}
