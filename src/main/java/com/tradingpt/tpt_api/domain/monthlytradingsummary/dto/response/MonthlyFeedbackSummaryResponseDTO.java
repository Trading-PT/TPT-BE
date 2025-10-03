package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "월별 통계 DTO")
public class MonthlyFeedbackSummaryResponseDTO {

	@Schema(description = "각 주차별 통계 DTO")
	private List<MonthlyWeekFeedbackSummaryResponseDTO> weekFeedbackSummaryResponseDTOS;

	@Schema(description = "월간 최종 승률")
	private Double winningRate;

	@Schema(description = "월간 평균 R&R")
	private Double monthlyAverageRnr;

	@Schema(description = "월간 최종 P&L")
	private BigDecimal monthlyPnl;

	public static MonthlyFeedbackSummaryResponseDTO of(
		List<MonthlyWeekFeedbackSummaryResponseDTO> weekFeedbackSummaryResponseDTOS,
		Double winningRate, Double monthlyAverageRnr, BigDecimal monthlyPnl) {
		return MonthlyFeedbackSummaryResponseDTO.builder()
			.weekFeedbackSummaryResponseDTOS(weekFeedbackSummaryResponseDTOS)
			.winningRate(winningRate)
			.monthlyAverageRnr(monthlyAverageRnr)
			.monthlyPnl(monthlyPnl)
			.build();
	}

}
