package com.tradingpt.tpt_api.domain.feedbackrequest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackType {
	SWING("스윙"),
	DAY("데이"),
	SCALPING("스켈핑");
	
	private final String description;
}
