package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.FeedbackSummaryBase;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

import lombok.Getter;

@Getter
public class DailyFeedbackSummary extends FeedbackSummaryBase {
	private final LocalDate date;

	public DailyFeedbackSummary(LocalDate date, Long totalCount, Status status) {
		super(totalCount, status);
		this.date = date;
	}
}