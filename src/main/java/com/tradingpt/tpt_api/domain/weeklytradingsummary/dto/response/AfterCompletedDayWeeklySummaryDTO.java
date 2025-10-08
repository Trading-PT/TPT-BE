package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "완강 후 고객 주별 요약 - 데이 트레이딩")
public class AfterCompletedDayWeeklySummaryDTO extends WeeklySummaryResponseDTO {
	
	@Schema(description = "주별 통계 DTO")
	private WeeklyFeedbackSummaryResponseDTO weeklyFeedbackSummaryResponseDTO;

	@Schema(description = "방향성에 대한 통계")
	private DirectionStatisticsResponseDTO directionStatisticsResponseDTO;

	@Schema(description = "회원님의 손실난 매매 분석")
	private String weeklyLossTradingAnalysis;

	@Schema(description = "회원님의 수익난 매매 분석")
	private String weeklyProfitableTradingAnalysis;

	@Schema(description = "회원님의 주간 매매 최종 평가")
	private String weeklyEvaluation;

}
