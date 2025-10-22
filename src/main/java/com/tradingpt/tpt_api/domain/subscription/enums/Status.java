package com.tradingpt.tpt_api.domain.subscription.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

	ACTIVE("활성 (정상 결제 중)"),
	CANCELLED("해지됨"),
	PAYMENT_FAILED("결제 실패"),
	EXPIRED("만료됨"),
	;

	private final String description;
}
