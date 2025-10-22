package com.tradingpt.tpt_api.domain.review.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

	PUBLIC("공개"),
	PRIVATE("비공개"),
	;

	private final String description;
}
