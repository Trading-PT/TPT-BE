package com.tradingpt.tpt_api.domain.feedbackrequest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

	FR("피드백 답변 완료, 피드백 답변 읽음"), // isResponded = true, isRead = true
	FN("피드백 답변 완료, 피드백 답변 읽지 않음"), // isResponded = true, isRead = false
	N("피드백 답변 미완료"); // isResponded = false, isRead = false

	private final String description;
}
