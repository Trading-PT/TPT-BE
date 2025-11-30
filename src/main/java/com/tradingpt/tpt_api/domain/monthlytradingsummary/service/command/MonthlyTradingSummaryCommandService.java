package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.command;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.CreateMonthlyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.UpsertMonthlyEvaluationRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyEvaluationResponseDTO;

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

	/**
	 * 월간 매매일지 트레이너 평가 Upsert (트레이너/관리자용 - 완강 후)
	 * 비즈니스 규칙: AFTER_COMPLETION일 때만 트레이너가 평가 작성 가능 (DAY/SWING 모두)
	 *
	 * @param year       연도
	 * @param month      월
	 * @param customerId 고객 ID
	 * @param trainerId  트레이너 ID
	 * @param request    평가 요청 DTO
	 * @return MonthlyEvaluationResponseDTO
	 */
	MonthlyEvaluationResponseDTO upsertMonthlyEvaluationByTrainer(
		Integer year,
		Integer month,
		Long customerId,
		Long trainerId,
		UpsertMonthlyEvaluationRequestDTO request
	);
}
