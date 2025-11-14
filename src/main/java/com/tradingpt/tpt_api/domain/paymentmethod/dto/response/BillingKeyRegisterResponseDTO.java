package com.tradingpt.tpt_api.domain.paymentmethod.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;

import lombok.Builder;
import lombok.Getter;

/**
 * 빌키 등록 완료 응답 DTO
 */
@Getter
@Builder
public class BillingKeyRegisterResponseDTO {

	/**
	 * 등록된 결제수단 ID
	 */
	private Long paymentMethodId;

	/**
	 * 빌링키
	 */
	private String billingKey;

	/**
	 * 카드사명
	 */
	private String cardName;

	/**
	 * 마스킹된 카드번호 (예: 123456******1234)
	 */
	private String cardNo;

	/**
	 * 카드사 코드
	 */
	private String cardCode;

	/**
	 * 빌링키 발급 일시
	 */
	private LocalDateTime issuedAt;

	/**
	 * 주 결제수단 여부
	 */
	private Boolean isPrimary;

	/**
	 * 활성 상태
	 */
	private Boolean isActive;

	public static BillingKeyRegisterResponseDTO from(PaymentMethod paymentMethod) {
		return BillingKeyRegisterResponseDTO.builder()
			.paymentMethodId(paymentMethod.getId())
			.billingKey(paymentMethod.getBillingKey())
			.cardName(paymentMethod.getCardCompanyName())
			.cardNo(paymentMethod.getMaskedIdentifier())
			.cardCode(paymentMethod.getCardCompanyCode())
			.issuedAt(paymentMethod.getBillingKeyIssuedAt())
			.isPrimary(paymentMethod.getIsPrimary())
			.isActive(paymentMethod.getIsActive())
			.build();
	}
}
