package com.tradingpt.tpt_api.domain.leveltest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelTestStaus {

	//제출은 됬지만 채점 전, (다른 서버 인스턴스가 중복 채점 방지를 위한) 채점 진행중,  채점 후
	SUBMITTED("제출은 됐지만 채점 전"),
	GRADING("채점 진행중"),
	GRADED("채점 후"),
	;

	private final String description;

}
