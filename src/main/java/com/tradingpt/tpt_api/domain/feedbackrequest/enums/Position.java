package com.tradingpt.tpt_api.domain.feedbackrequest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Position {

	LONG("롱"),
	SHORT("숏");

	private final String description;
}
