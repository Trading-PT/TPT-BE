package com.tradingpt.tpt_api.domain.weeklytradingsummary.service.command;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.CreateWeeklyTradingSummaryRequestDTO;

public interface WeeklyTradingSummaryCommandService {

	/**
	 * 주간 매매 일지 통계 생성 (Admin/Trainer)
	 * - 완강 전: 생성 불가 (customer가 작성)
	 * - 완강 후 + DAY: 상세 평가 3개 작성
	 * - 완강 후 + SCALPING/SWING: 생성 불가
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
}