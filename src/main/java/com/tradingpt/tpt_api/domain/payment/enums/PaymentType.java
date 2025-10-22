package com.tradingpt.tpt_api.domain.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {

	INITIAL("최초 구독 결제"),
	RECURRING("정기 자동 결제"),
	MANUAL("수동 결제 (관리자/재시도)"),
	;

	private final String description;
}
