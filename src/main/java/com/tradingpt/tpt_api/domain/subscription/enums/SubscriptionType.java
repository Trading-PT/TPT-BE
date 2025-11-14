package com.tradingpt.tpt_api.domain.subscription.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionType {

	REGULAR("일반"),
	PRE_REGISTER("사전 등록"),
	;

	private final String description;
}
