package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.FeedbackSummaryBase;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

import lombok.Getter;

/**
 * 연도별 월간 피드백 요약
 */
@Getter
public class MonthlyFeedbackSummary extends FeedbackSummaryBase {
	private final Integer month; // 월

	public MonthlyFeedbackSummary(Integer month, Long totalCount, Status status) {
		super(totalCount, status);  // 부모 생성자 호출
		this.month = month;
	}
}