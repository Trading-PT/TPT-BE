package com.tradingpt.tpt_api.domain.paymentmethod.dto.response;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.enums.CardType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 결제수단 상세 응답 DTO
 */
@Getter
@Builder
public class PaymentMethodResponse {

    /**
     * 결제수단 ID
     */
    private Long id;

    /**
     * 화면 표시명 (예: 신한카드 ****1234)
     */
    private String displayName;

    /**
     * 카드사명
     */
    private String cardCompanyName;

    /**
     * 카드사 코드
     */
    private String cardCompanyCode;

    /**
     * 마스킹된 카드번호
     */
    private String maskedCardNo;

    /**
     * 카드 타입 (CREDIT, DEBIT, SIMPLE)
     */
    private CardType cardType;

    /**
     * 주 결제수단 여부
     */
    private Boolean isPrimary;

    /**
     * 활성 상태
     */
    private Boolean isActive;

    /**
     * 만료일
     */
    private LocalDate expiresAt;

    /**
     * 만료 여부
     */
    private Boolean isExpired;

    /**
     * 빌링키 발급 일시
     */
    private LocalDateTime billingKeyIssuedAt;

    /**
     * 등록 일시
     */
    private LocalDateTime createdAt;

    /**
     * PaymentMethod 엔티티로부터 DTO 생성
     */
    public static PaymentMethodResponse from(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
            .id(paymentMethod.getId())
            .displayName(paymentMethod.getDisplayName())
            .cardCompanyName(paymentMethod.getCardCompanyName())
            .cardCompanyCode(paymentMethod.getCardCompanyCode())
            .maskedCardNo(paymentMethod.getMaskedIdentifier())
            .cardType(paymentMethod.getCardType())
            .isPrimary(paymentMethod.getIsPrimary())
            .isActive(paymentMethod.getIsActive())
            .expiresAt(paymentMethod.getExpiresAt())
            .isExpired(paymentMethod.isExpired())
            .billingKeyIssuedAt(paymentMethod.getBillingKeyIssuedAt())
            .createdAt(paymentMethod.getCreatedAt())
            .build();
    }
}
