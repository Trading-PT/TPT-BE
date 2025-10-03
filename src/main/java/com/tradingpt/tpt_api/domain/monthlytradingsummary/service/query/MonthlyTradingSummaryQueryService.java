package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.query;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlySummaryResponseDTO;

import java.util.List;

public interface MonthlyTradingSummaryQueryService {
	/**
	 * 각 연도별 고객이 피드백을 남긴 월 보기
	 * @param year
	 * @param customerId
	 * @return
	 */
	YearlySummaryResponseDTO getYearlySummaryResponse(Integer year, Long customerId);

	/**
	 * 연/월에 대한 CourseStatus별 매매 일지 통계 보기
	 * @param year
	 * @param month
	 * @param customerId
	 * @return CourseStatus별 통계 목록 (한 달에 여러 CourseStatus 존재 가능)
	 */
	List<MonthlySummaryResponseDTO> getMonthlySummaryResponse(Integer year, Integer month, Long customerId);
}
