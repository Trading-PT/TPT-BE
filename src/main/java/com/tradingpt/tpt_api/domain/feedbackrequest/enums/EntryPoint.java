package com.tradingpt.tpt_api.domain.feedbackrequest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EntryPoint {

	REVERSE("리버스"),
	PULL_BACK("풀백"),
	BREAK_OUT("브레이크 아웃");
	
	private final String description;
}
