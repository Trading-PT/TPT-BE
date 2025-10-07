package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import java.math.BigDecimal;

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
@Schema(description = "월별 성과 비교")
public class MonthlyPerformanceComparison {

	private MonthSnapshot beforeMonth;
	private MonthSnapshot currentMonth;

	public static MonthlyPerformanceComparison of(
		MonthSnapshot beforeMonth, MonthSnapshot currentMonth
	) {
		return MonthlyPerformanceComparison.builder()
			.beforeMonth(beforeMonth)
			.currentMonth(currentMonth)
			.build();
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

		public static MonthSnapshot of(Integer month, BigDecimal finalWinRate, BigDecimal averageRoi,
			BigDecimal finalPnL) {
			return MonthSnapshot.builder()
				.month(month)
				.finalWinRate(finalWinRate)
				.averageRoi(averageRoi)
				.finalPnL(finalPnL)
				.build();
		}
	}
}
