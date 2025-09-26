package com.tradingpt.tpt_api.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseStatus {

	BEFORE_COMPLETION("완강 전"),
	AFTER_COMPLETION("완강 후"),
	;

	private final String description;
	
}
