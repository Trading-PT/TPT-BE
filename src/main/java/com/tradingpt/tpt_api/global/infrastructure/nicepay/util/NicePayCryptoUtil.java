package com.tradingpt.tpt_api.global.infrastructure.nicepay.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * NicePay API 암호화 유틸리티 클래스
 * SHA-256 해시 생성 및 고유 식별자 생성 기능 제공
 */
public class NicePayCryptoUtil {

    private static final DateTimeFormatter EDI_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * SHA-256 해시를 16진수 문자열로 생성합니다.
     * NicePay API의 SignData 생성에 사용됩니다.
     *
     * @param params 해시할 파라미터들 (순서대로 연결됨)
     * @return 16진수 형식의 SHA-256 해시
     * @throws RuntimeException SHA-256 알고리즘을 사용할 수 없는 경우
     */
    public static String generateSignData(String... params) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String param : params) {
                sb.append(param);
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            // byte 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * NicePay API용 EdiDate 생성
     * 형식: yyyyMMddHHmmss
     *
     * @return 현재 시간의 EdiDate 문자열
     */
    public static String generateEdiDate() {
        return LocalDateTime.now().format(EDI_DATE_FORMATTER);
    }

    /**
     * 빌키 발급용 고유 Moid(주문번호) 생성
     * 형식: BK-{UUID}
     *
     * @return 고유한 Moid 문자열
     */
    public static String generateMoid() {
        return "BK-" + UUID.randomUUID().toString();
    }
}
