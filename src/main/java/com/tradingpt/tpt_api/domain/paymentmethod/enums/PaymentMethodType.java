package com.tradingpt.tpt_api.domain.paymentmethod.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethodType {

	CARD("카드"),
	KAKAOPAY("카카오페이"),
	NAVERPAY("네이버페이"),
	;

	private final String description;
}
