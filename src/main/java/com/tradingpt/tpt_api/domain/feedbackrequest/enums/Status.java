package com.tradingpt.tpt_api.domain.feedbackrequest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

	FR("피드백 답변 완료, 피드백 읽음"),
	FN("피드백 답변 완료, 피드백 읽지 않음"),
	N("피드백 답변 미완료");

	private final String description;
}
