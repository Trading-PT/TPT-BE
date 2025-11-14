package com.tradingpt.tpt_api.domain.paymentmethod.service.query;

import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.PaymentMethodResponse;

/**
 * PaymentMethod Query Service 인터페이스
 * 결제수단 조회 작업을 담당
 */
public interface PaymentMethodQueryService {

	/**
	 * 고객의 주 결제수단 조회
	 *
	 * @param customerId 고객 ID
	 * @return 주 결제수단 (없으면 null)
	 */
	PaymentMethodResponse getPrimaryPaymentMethod(Long customerId);

	/**
	 * 특정 결제수단 상세 조회
	 *
	 * @param customerId      고객
	 * @param paymentMethodId 결제수단 ID
	 * @return 결제수단 상세 정보
	 */
	PaymentMethodResponse getPaymentMethod(Long customerId, Long paymentMethodId);
}
