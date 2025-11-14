package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * NicePay 빌키 삭제 API 요청 DTO
 * API: /webapi/billing/billkey_remove.jsp
 * Content-Type: application/x-www-form-urlencoded; charset=EUC-KR
 */
@Getter
@Builder
public class BillingKeyDeleteRequestDTO {

    /**
     * 상점 ID
     * 필수, 10 bytes
     */
    private String MID;

    /**
     * 빌키 (삭제할 빌링키)
     * 필수, 30 bytes
     */
    private String BID;

    /**
     * 전문 생성 일시 (yyyyMMddHHmmss)
     * 필수, 14 bytes
     */
    private String EdiDate;

    /**
     * 위변조 검증 데이터
     * SHA256(MID + EdiDate + BID + MerchantKey)
     * 필수, 256 bytes
     */
    private String SignData;
}
