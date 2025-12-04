package com.tradingpt.tpt_api.domain.feedbackrequest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Grade {

	S_PLUS("S+"),
	S("S"),
	A("A"),
	B("B"),
	C("C"),
	FREE("재량"),
	NONE("*");

	private final String description;
}
