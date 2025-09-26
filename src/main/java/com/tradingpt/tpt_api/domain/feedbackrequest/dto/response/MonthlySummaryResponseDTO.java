package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

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
@Schema(description = "월별 트레이딩 피드백 DTO")
public class MonthlySummaryResponseDTO {

	@Schema(description = "고객의 완강 여부")
	private Boolean isCourseCompleted;

	@Schema(description = "각 주차별 통계 DTO")
	private List<WeeklyFeedbackSummaryDTO> weeklyFeedbackSummaryDTOS;

	@Schema(description = "월간 최종 승률")
	private Integer monthlyFinalWinRatio;

	@Schema(description = "월간 평균 손익비")
	private Integer monthlyAverageWinLossRatio;

	@Schema(description = "월간 최종 P&L")
	private Integer monthlyFinalPnl;

	@Schema(description = "한 달 간 매매 최종 평가")
	private String monthlyEvaluation;

	@Schema(description = "다음 달 목표 성과")
	private String nextMonthGoal;

	@Schema(description = "트레이너 평가 완료 여부")
	private Boolean isTrainerEvaluated;

	@Schema(description = "매매 성적 변화량")
	private TradingPerformanceVariation tradingPerformanceVariation;

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	@Schema(description = "각 주차별 통계 DTO")
	public static class WeeklyFeedbackSummaryDTO {

		@Schema(description = "몇 주차")
		private Integer feedbackWeek;

		@Schema(description = "주간 매매 횟수")
		private Integer tradingCount;

		@Schema(description = "주간 P&L")
		private Integer weeklyPnl;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "매매 성적 변화량")
	public static class TradingPerformanceVariation {
		private MonthSnapshot beforeMonth;
		private MonthSnapshot currentMonth;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MonthSnapshot {
		private Integer month;          // e.g. 7 (7월)
		private BigDecimal finalWinRate;
		private BigDecimal averageRoi;
		private BigDecimal finalPnL;
	}
}
