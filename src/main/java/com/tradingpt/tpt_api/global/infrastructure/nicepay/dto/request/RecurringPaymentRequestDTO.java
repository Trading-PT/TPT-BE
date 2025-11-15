package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * NicePay 빌링 결제(승인) API 요청 DTO
 * API: /webapi/billing/billing_approve.jsp
 * Content-Type: application/x-www-form-urlencoded; charset=EUC-KR
 */
@Getter
@Builder
public class RecurringPaymentRequestDTO {

    /**
     * 빌링키 (사전 발급된 BID)
     * 필수, 30 bytes
     */
    private String BID;

    /**
     * 상점 ID
     * 필수, 10 bytes
     */
    private String MID;

    /**
     * 거래 ID (반드시 새로 생성)
     * TID 생성 규칙: MID(10) + 지불수단(2) + 매체구분(2) + 시간정보(12) + 랜덤(4)
     * 예: nicepay00m01162505211322240123
     * 필수, 30 bytes
     */
    private String TID;

    /**
     * 전문 생성 일시 (YYYYMMDDHHMMSS)
     * 필수, 14 bytes
     */
    private String EdiDate;

    /**
     * 주문번호 (상점에서 관리하는 고유 주문번호)
     * 필수, 64 bytes
     */
    private String Moid;

    /**
     * 결제 금액 (숫자만)
     * 필수, 12 bytes
     */
    private String Amt;

    /**
     * 상품명
     * 필수, 40 bytes
     */
    private String GoodsName;

    /**
     * 위변조 검증 데이터
     * SHA256(MID + EdiDate + Moid + Amt + BID + MerchantKey)
     * 필수, 256 bytes
     */
    private String SignData;

    /**
     * 무이자 할부 여부
     * 0: 일반, 1: 무이자
     * 필수, 1 byte
     */
    @Builder.Default
    private String CardInterest = "0";

    /**
     * 할부 개월수
     * 00: 일시불, 02-12: 할부 개월
     * 필수, 2 bytes
     */
    @Builder.Default
    private String CardQuota = "00";
}
