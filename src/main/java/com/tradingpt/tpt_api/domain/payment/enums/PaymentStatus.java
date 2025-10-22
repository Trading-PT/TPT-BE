package com.tradingpt.tpt_api.domain.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {

	PENDING("결제 대기"),
	SUCCESS("결제 성공"),
	FAILED("결제 실패"),
	CANCELLED("결제 취소"),
	REFUNDED("환불 완료"),
	;

	private final String description;
}
