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
public class AfterCompletedDayWeeklySummaryDTO {

	@Schema(description = "주별 통계 DTO")
	private WeeklyFeedbackSummaryResponseDTO weeklyFeedbackSummaryResponseDTO;

	private DirectionStatisticsResponseDTO directionStatisticsResponseDTO;

}
