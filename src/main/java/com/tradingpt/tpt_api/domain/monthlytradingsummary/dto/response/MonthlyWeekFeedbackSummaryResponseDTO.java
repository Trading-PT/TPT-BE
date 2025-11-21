package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

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

	@Schema(description = "주차", example = "3")
	private Integer week;

	@Schema(description = "주차 시작일", example = "2025-11-10")
	private LocalDate startDate;

	@Schema(description = "주차 종료일", example = "2025-11-16")
	private LocalDate endDate;

	@Schema(description = "매매 횟수")
	private Integer tradingCount;

	@Schema(description = "주간 P&L")
	private BigDecimal weeklyPnl;

	@Schema(description = "피드백 답변 읽음 상태 여부")
	private Status status;

	public static MonthlyWeekFeedbackSummaryResponseDTO of(Integer week, LocalDate startDate, LocalDate endDate,
		Integer tradingCount, BigDecimal weeklyPnl, Status status) {
		return MonthlyWeekFeedbackSummaryResponseDTO.builder()
			.week(week)
			.startDate(startDate)
			.endDate(endDate)
			.tradingCount(tradingCount)
			.weeklyPnl(weeklyPnl)
			.status(status)
			.build();
	}

}
