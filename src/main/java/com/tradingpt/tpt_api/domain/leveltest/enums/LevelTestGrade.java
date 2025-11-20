package com.tradingpt.tpt_api.domain.leveltest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelTestGrade {
	A("A"),
	B("B"),
	C("C"),
	;

	private final String description;

}
