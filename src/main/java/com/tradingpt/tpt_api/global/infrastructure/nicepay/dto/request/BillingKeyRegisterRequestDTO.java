package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * NicePay 빌키 발급 API 요청 DTO
 * API: /webapi/billing/cardbill_regist.jsp
 * Content-Type: application/x-www-form-urlencoded; charset=EUC-KR
 */
@Getter
@Builder
public class BillingKeyRegisterRequestDTO {

    /**
     * 거래 ID (NicePay 인증 응답에서 받은 TxTid)
     * 필수, 30 bytes
     */
    private String TID;

    /**
     * 상점 ID
     * 필수, 10 bytes
     */
    private String MID;

    /**
     * 인증 토큰 (NicePay 인증 응답에서 받은 AuthToken)
     * 필수, 40 bytes
     */
    private String AuthToken;

    /**
     * 전문 생성 일시 (yyyyMMddHHmmss)
     * 필수, 14 bytes
     */
    private String EdiDate;

    /**
     * 위변조 검증 데이터
     * SHA256(TID + MID + EdiDate + MerchantKey)
     * 필수, 256 bytes
     */
    private String SignData;
}
