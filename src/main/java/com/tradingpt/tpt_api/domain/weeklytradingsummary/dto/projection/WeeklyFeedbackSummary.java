package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.FeedbackSummaryBase;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

import lombok.Getter;

/**
 * 월별 주간 피드백 요약
 */
@Getter
public class WeeklyFeedbackSummary extends FeedbackSummaryBase {
	private final Integer week; // 주

	public WeeklyFeedbackSummary(Integer week, Long totalCount, Status status) {
		super(totalCount, status);
		this.week = week;
	}
}
