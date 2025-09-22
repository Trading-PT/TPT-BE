package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlySummaryResponseDTO;

public interface FeedbackRequestCalendarQueryService {
	MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Long customerId);
}
