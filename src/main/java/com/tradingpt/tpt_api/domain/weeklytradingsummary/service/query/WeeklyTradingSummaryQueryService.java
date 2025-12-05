package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.DailyFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyDayFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyLossFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyProfitFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklySummaryResponseDTO;

public interface WeeklyTradingSummaryQueryService {

	WeeklySummaryResponseDTO getWeeklyTradingSummary(Integer year, Integer month, Integer week, Long customerId);

	/**
	 * 특정 주의 피드백이 존재하는 날짜 목록 조회
	 *
	 * <p>권한:
	 * <ul>
	 *   <li>ROLE_ADMIN: 모든 고객의 피드백 조회 가능</li>
	 *   <li>ROLE_TRAINER: 담당 고객의 피드백만 조회 가능</li>
	 * </ul>
	 *
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param customerId 고객 ID
	 * @param userId 사용자 ID (Admin 또는 Trainer)
	 * @return 피드백이 존재하는 날짜 목록
	 */
	WeeklyDayFeedbackResponseDTO getWeeklyDayFeedback(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		Long userId
	);

	/**
	 * 특정 날짜의 피드백 목록 조회
	 *
	 * <p>권한:
	 * <ul>
	 *   <li>ROLE_ADMIN: 모든 고객의 피드백 조회 가능</li>
	 *   <li>ROLE_TRAINER: 담당 고객의 피드백만 조회 가능</li>
	 * </ul>
	 *
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param day 일
	 * @param customerId 고객 ID
	 * @param userId 사용자 ID (Admin 또는 Trainer)
	 * @return 특정 날짜의 피드백 목록
	 */
	DailyFeedbackListResponseDTO getDailyFeedbackList(
		Integer year,
		Integer month,
		Integer week,
		Integer day,
		Long customerId,
		Long userId
	);

	/**
	 * 특정 주의 이익 매매 피드백 목록 조회 (완강 전)
	 *
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param customerId 고객 ID
	 * @return 이익 매매 피드백 목록
	 */
	WeeklyProfitFeedbackListResponseDTO getProfitFeedbacksByWeek(
		Integer year,
		Integer month,
		Integer week,
		Long customerId
	);

	/**
	 * 특정 주의 손실 매매 피드백 목록 조회 (완강 전)
	 *
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @param customerId 고객 ID
	 * @return 손실 매매 피드백 목록
	 */
	WeeklyLossFeedbackListResponseDTO getLossFeedbacksByWeek(
		Integer year,
		Integer month,
		Integer week,
		Long customerId
	);
}
