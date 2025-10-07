package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "완강 후 고객 월별 요약 - 스윙/데이 트레이딩")
public class AfterCompletedGeneralMonthlySummaryDTO extends MonthlySummaryResponseDTO {

	@Schema(description = "월별 통계 DTO")
	private MonthlyFeedbackSummaryResponseDTO monthlyFeedbackSummaryResponseDTO;

	@Schema(description = "트레이너 평가 완료 여부")
	private Boolean isTrainerEvaluated;

	@Schema(description = "한 달 간 매매 최종 평가")
	private String monthlyEvaluation;

	@Schema(description = "다음 달 목표 성과")
	private String nextMonthGoal;

	@Schema(description = "진입 타점에 대한 통계")
	private EntryPointStatisticsResponseDTO entryPointStatisticsResponseDTO;

	@Schema(description = "월별 성과 비교")
	private MonthlyPerformanceComparison tradingPerformanceVariation;

	public static AfterCompletedGeneralMonthlySummaryDTO of(
		MonthlyFeedbackSummaryResponseDTO monthlyFeedbackSummaryResponseDTO,
		Boolean isTrainerEvaluated, String monthlyEvaluation, String nextMonthGoal,
		EntryPointStatisticsResponseDTO entryPointStatisticsResponseDTO,
		MonthlyPerformanceComparison tradingPerformanceVariation
	) {
		return AfterCompletedGeneralMonthlySummaryDTO.builder()
			.monthlyFeedbackSummaryResponseDTO(monthlyFeedbackSummaryResponseDTO)
			.isTrainerEvaluated(isTrainerEvaluated)
			.monthlyEvaluation(monthlyEvaluation)
			.nextMonthGoal(nextMonthGoal)
			.entryPointStatisticsResponseDTO(entryPointStatisticsResponseDTO)
			.tradingPerformanceVariation(tradingPerformanceVariation)
			.build();
	}
}
