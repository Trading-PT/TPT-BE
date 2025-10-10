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
public class PerformanceComparison<T> {

	private T before;
	private T current;

	public static PerformanceComparison<MonthSnapshot> of(
		MonthSnapshot beforeMonth, MonthSnapshot currentMonth
	) {
		return PerformanceComparison.<MonthSnapshot>builder()
			.before(beforeMonth)
			.current(currentMonth)
			.build();
	}

	public static PerformanceComparison<WeekSnapshot> of(
		WeekSnapshot beforeWeek, WeekSnapshot currentWeek
	) {
		return PerformanceComparison.<WeekSnapshot>builder()
			.before(beforeWeek)
			.current(currentWeek)
			.build();
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MonthSnapshot {
		private Integer month;          // e.g. 7 (7월)
		private BigDecimal finalWinRate;
		private BigDecimal averageRnr;
		private BigDecimal finalPnL;

		public static MonthSnapshot of(Integer month, BigDecimal finalWinRate, BigDecimal averageRnr,
			BigDecimal finalPnL) {
			return MonthSnapshot.builder()
				.month(month)
				.finalWinRate(finalWinRate)
				.averageRnr(averageRnr)
				.finalPnL(finalPnL)
				.build();
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class WeekSnapshot {
		private Integer week;
		private Double winRate;
		private Double rnr;
		private BigDecimal pnl;

		public static WeekSnapshot of(Integer week, Double winRate, Double rnr, BigDecimal pnl) {
			return WeekSnapshot.builder()
				.week(week)
				.winRate(winRate)
				.rnr(rnr)
				.pnl(pnl)
				.build();
		}
	}
}
