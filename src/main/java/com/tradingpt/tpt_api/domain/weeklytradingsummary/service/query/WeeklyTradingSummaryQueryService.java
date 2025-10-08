package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklySummaryResponseDTO;

public interface WeeklyTradingSummaryQueryService {

	WeeklySummaryResponseDTO getWeeklyTradingSummary(Integer year, Integer month, Integer week, Long customerId);
}
