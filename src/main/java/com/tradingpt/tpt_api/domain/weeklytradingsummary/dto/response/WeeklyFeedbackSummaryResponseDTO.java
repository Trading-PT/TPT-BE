package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

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
@Schema(description = "주별 통계 DTO")
public class WeeklyFeedbackSummaryResponseDTO {

	@Schema(description = "각 일별 통계 DTO")
	private List<WeeklyWeekFeedbackSummaryResponseDTO> weeklyWeekFeedbackSummaryResponseDTOS;

	@Schema(description = "주간 최종 승률")
	private Double winningRate;

	@Schema(description = "주간 평균 R&R")
	private Double weeklyAverageRnr;

	@Schema(description = "주간 최종 P&L")
	private BigDecimal weeklyPnl;

}
