package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.command;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.CreateMonthlyTradingSummaryRequestDTO;

public interface MonthlyTradingSummaryCommandService {

	/**
	 * 월별 매매 일지 답변 생성
	 * @param year
	 * @param month
	 * @param customerId
	 * @param request
	 * @return
	 */
	Void createMonthlySummary(Integer year, Integer month, Long customerId,
		CreateMonthlyTradingSummaryRequestDTO request);
	
}
