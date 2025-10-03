package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DailyFeedbackRequestsResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlySummaryResponseDTO;

public interface FeedbackRequestJournalQueryService {
	YearlySummaryResponseDTO getYearlySummaryResponse(Integer year, Long customerId);

	DailyFeedbackRequestsResponseDTO getDailyFeedbackRequestsResponse(Integer year, Integer month, Integer day,
		Long customerId);

	MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Integer month, Long customerId);

}
