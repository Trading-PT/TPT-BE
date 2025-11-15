package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NicePay 빌링 결제(승인) API 응답 DTO
 *
 * 성공 응답 예시:
 * ResultCode=3001
 * ResultMsg=정상처리
 * TID=nictest04m01162501141234567890
 * Moid=order_202501141234567890
 * Amt=50000
 * AuthCode=123456
 * AuthDate=20250114123456
 * CardCode=01
 * CardName=신한카드
 * CardNo=123456******1234
 * CardQuota=00
 */
@Getter
@Setter
@NoArgsConstructor
public class RecurringPaymentResponseDTO {

    /**
     * 결과 코드
     * 3001: 정상
     * 기타: 에러
     */
    private String ResultCode;

    /**
     * 결과 메시지
     */
    private String ResultMsg;

    /**
     * 거래 ID
     * 30 bytes
     */
    private String TID;

    /**
     * 주문번호
     * 64 bytes
     */
    private String Moid;

    /**
     * 승인 금액
     * 12 bytes
     */
    private String Amt;

    /**
     * 승인 번호
     * 12 bytes
     */
    private String AuthCode;

    /**
     * 승인 일시 (YYYYMMDDHHmmss)
     * 14 bytes
     */
    private String AuthDate;

    /**
     * 카드사 코드
     * 2 bytes
     */
    private String CardCode;

    /**
     * 카드사 명
     * 20 bytes
     */
    private String CardName;

    /**
     * 카드번호 (마스킹 처리됨)
     * 20 bytes
     */
    private String CardNo;

    /**
     * 할부 개월수
     * 00: 일시불
     * 2 bytes
     */
    private String CardQuota;

    /**
     * 카드 타입
     * 0: 신용카드, 1: 체크카드
     * 1 byte
     */
    private String CardCl;

    /**
     * 영수증 URL (선택)
     */
    private String ReceiptUrl;

    /**
     * 응답이 성공인지 확인
     *
     * @return 3001이면 true
     */
    public boolean isSuccess() {
        return "3001".equals(ResultCode);
    }

    /**
     * 승인 일시를 LocalDateTime으로 변환
     * 형식: YYYYMMDDHHmmss → LocalDateTime
     */
    public LocalDateTime getAuthDateAsLocalDateTime() {
        if (AuthDate == null || AuthDate.length() != 14) {
            return null;
        }
        try {
            int year = Integer.parseInt(AuthDate.substring(0, 4));
            int month = Integer.parseInt(AuthDate.substring(4, 6));
            int day = Integer.parseInt(AuthDate.substring(6, 8));
            int hour = Integer.parseInt(AuthDate.substring(8, 10));
            int minute = Integer.parseInt(AuthDate.substring(10, 12));
            int second = Integer.parseInt(AuthDate.substring(12, 14));
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception e) {
            return null;
        }
    }
}
