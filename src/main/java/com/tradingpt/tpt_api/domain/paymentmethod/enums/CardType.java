package com.tradingpt.tpt_api.domain.paymentmethod.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CardType {

	CREDIT("신용카드"),
	DEBIT("체크카드"),
	SIMPLE("간편 결제"),
	;

	private final String description;
}
