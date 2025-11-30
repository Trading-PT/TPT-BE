package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.command;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.CreateWeeklyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.UpsertWeeklyEvaluationRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.UpsertWeeklyMemoRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyEvaluationResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyMemoResponseDTO;

public interface WeeklyTradingSummaryCommandService {

	/**
	 * 주간 매매 일지 통계 생성 (Admin/Trainer)
	 * - 완강 전: 생성 불가 (customer가 작성)
	 * - 완강 후 + DAY: 상세 평가 3개 작성
	 * - 완강 후 + SWING: 생성 불가
	 */
	Void createWeeklyTradingSummaryByTrainer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		Long trainerId,
		CreateWeeklyTradingSummaryRequestDTO request
	);

	/**
	 * 주간 매매 일지 통계 생성 (Customer)
	 * - 완강 전: memo 작성
	 * - 완강 후: 생성 불가
	 */
	Void createWeeklyTradingSummaryByCustomer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		CreateWeeklyTradingSummaryRequestDTO request
	);

	/**
	 * 주간 매매일지 메모 Upsert (고객용 - 완강 전)
	 * 비즈니스 규칙: BEFORE_COMPLETION 그룹(BEFORE_COMPLETION, PENDING_COMPLETION)일 때만 고객이 메모 작성 가능
	 *
	 * @param year       연도
	 * @param month      월
	 * @param week       주차
	 * @param customerId 고객 ID
	 * @param request    메모 요청 DTO
	 * @return WeeklyMemoResponseDTO
	 */
	WeeklyMemoResponseDTO upsertWeeklyMemoByCustomer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		UpsertWeeklyMemoRequestDTO request
	);

	/**
	 * 주간 매매일지 트레이너 평가 Upsert (트레이너/관리자용 - 완강 후 + DAY 타입)
	 * 비즈니스 규칙: AFTER_COMPLETION + DAY 타입일 때만 트레이너가 평가 작성 가능
	 *
	 * @param year       연도
	 * @param month      월
	 * @param week       주차
	 * @param customerId 고객 ID
	 * @param trainerId  트레이너 ID
	 * @param request    평가 요청 DTO
	 * @return WeeklyEvaluationResponseDTO
	 */
	WeeklyEvaluationResponseDTO upsertWeeklyEvaluationByTrainer(
		Integer year,
		Integer month,
		Integer week,
		Long customerId,
		Long trainerId,
		UpsertWeeklyEvaluationRequestDTO request
	);
}