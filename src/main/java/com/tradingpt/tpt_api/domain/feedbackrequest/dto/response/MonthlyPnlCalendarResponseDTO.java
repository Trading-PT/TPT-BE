package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 월별 PnL 달력 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "월별 PnL 달력")
public class MonthlyPnlCalendarResponseDTO {

	@Schema(description = "연도", example = "2025")
	private Integer year;

	@Schema(description = "월", example = "9")
	private Integer month;

	@Schema(description = "일별 PnL 목록")
	private List<DailyPnlDTO> dailyPnls;

	@Schema(description = "월 전체 PnL 합계", example = "500000.00")
	private BigDecimal totalPnl;

	@Schema(description = "월 평균 PnL 퍼센테이지", example = "3.5")
	private Double averagePnlPercentage;
}