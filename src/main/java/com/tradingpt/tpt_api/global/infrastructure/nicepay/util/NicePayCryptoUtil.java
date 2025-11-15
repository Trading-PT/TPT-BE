package com.tradingpt.tpt_api.global.infrastructure.nicepay.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * NicePay API 암호화 유틸리티 클래스
 * SHA-256 해시 생성, AES 암호화 및 고유 식별자 생성 기능 제공
 */
public class NicePayCryptoUtil {

    private static final DateTimeFormatter EDI_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter TID_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

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

    /**
     * AES-128/ECB/PKCS5Padding 암호화 (비인증 빌키 발급용)
     * 카드 정보를 EncData로 암호화합니다.
     *
     * @param plainText 암호화할 평문 (예: "CardNo=1234&ExpYear=25&ExpMonth=12&IDNo=900101&CardPw=12")
     * @param merchantKey 가맹점 키 (전체)
     * @return Hex 인코딩된 암호화 문자열
     * @throws RuntimeException 암호화 실패 시
     */
    public static String encryptAES(String plainText, String merchantKey) {
        try {
            // 가맹점 키의 앞 16자리를 AES Key로 사용
            String aesKey = merchantKey.substring(0, 16);

            // AES Key 생성
            SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");

            // Cipher 초기화 (AES/ECB/PKCS5Padding)
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // 암호화
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Hex 인코딩
            return bytesToHex(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    /**
     * TID(거래번호) 생성
     * 형식: MID(10) + 지불수단(2) + 매체구분(2) + 시간정보(yyMMddHHmmss) + 랜덤(4)
     * 예시: nicepay00m01162505211322240123
     *
     * @param mid 가맹점 ID (10자리)
     * @return 생성된 TID (30자리)
     */
    public static String generateTID(String mid) {
        if (mid == null || mid.length() != 10) {
            throw new IllegalArgumentException("MID must be exactly 10 characters");
        }

        // 지불수단: 01 = CARD (신용카드)
        String paymentMethod = "01";

        // 매체구분: 16 = 빌링결제
        String mediaType = "16";

        // 시간정보: yyMMddHHmmss (12자리)
        String timeInfo = LocalDateTime.now().format(TID_DATE_FORMATTER);

        // 랜덤 4자리 숫자
        String random = String.format("%04d", (int) (Math.random() * 10000));

        return mid + paymentMethod + mediaType + timeInfo + random;
    }

    /**
     * 정기 결제용 Moid(주문번호) 생성
     * 형식: SUB-{yyyyMMddHHmmss}-{UUID 앞 8자리}
     *
     * @param subscriptionId 구독 ID
     * @return 고유한 Moid 문자열
     */
    public static String generateRecurringMoid(Long subscriptionId) {
        String timestamp = LocalDateTime.now().format(EDI_DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("SUB%d-%s-%s", subscriptionId, timestamp, uuid);
    }

    /**
     * byte 배열을 Hex 문자열로 변환
     *
     * @param bytes byte 배열
     * @return Hex 문자열
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
