package com.tradingpt.tpt_api.domain.paymentmethod.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제수단 목록 응답 DTO
 */
@Getter
@Builder
public class PaymentMethodListResponseDTO {

	/**
	 * 결제수단 목록
	 */
	private List<PaymentMethodResponse> paymentMethods;

	/**
	 * 전체 결제수단 개수
	 */
	private Integer totalCount;

	public static PaymentMethodListResponseDTO of(List<PaymentMethodResponse> paymentMethods) {
		return PaymentMethodListResponseDTO.builder()
			.paymentMethods(paymentMethods)
			.totalCount(paymentMethods.size())
			.build();
	}
}
