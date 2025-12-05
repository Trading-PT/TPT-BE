package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.query;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyWeekFeedbackResponseDTO;

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
	MonthlySummaryResponseDTO getMonthlySummaryResponse(Integer year, Integer month, Long customerId);

	/**
	 * 특정 연/월에 피드백 요청이 존재하는 주차 목록 조회
	 *
	 * 역할별 동작:
	 * - ADMIN: 모든 고객의 피드백 주차 목록 조회 가능
	 * - TRAINER: 담당 고객의 피드백 주차 목록만 조회 가능
	 *
	 * @param year 연도
	 * @param month 월
	 * @param customerId 고객 ID
	 * @param userId 현재 로그인한 사용자 ID (ADMIN 또는 TRAINER)
	 * @return 피드백이 존재하는 주차 목록
	 */
	MonthlyWeekFeedbackResponseDTO getMonthlyWeekFeedbackResponse(
		Integer year,
		Integer month,
		Long customerId,
		Long userId
	);

}
