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
@Schema(description = "각 주차별 통계 DTO")
public class MonthlyWeekFeedbackSummaryResponseDTO {

	@Schema(description = "주차")
	private Integer week;

	@Schema(description = "매매 횟수")
	private Integer tradingCount;

	@Schema(description = "주간 P&L")
	private BigDecimal weeklyPnl;

	public static MonthlyWeekFeedbackSummaryResponseDTO of(Integer week, Integer tradingCount, BigDecimal weeklyPnl) {
		return MonthlyWeekFeedbackSummaryResponseDTO.builder()
			.week(week)
			.tradingCount(tradingCount)
			.weeklyPnl(weeklyPnl)
			.build();
	}
}
