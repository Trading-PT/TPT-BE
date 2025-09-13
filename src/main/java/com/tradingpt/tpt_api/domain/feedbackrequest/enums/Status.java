package com.tradingpt.tpt_api.domain.feedbackrequest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

	DONE("피드백 답변 완료"),
	NOT_YET("피드백 답변 미완료");

	private final String description;
}
