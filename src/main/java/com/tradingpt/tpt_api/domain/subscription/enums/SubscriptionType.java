package com.tradingpt.tpt_api.domain.subscription.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionType {

	REGULAR("일반"),
	PROMOTION("프로모션"),
	;

	private final String description;
}
