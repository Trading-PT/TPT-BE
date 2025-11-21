package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 일별 PnL DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "일별 PnL 정보")
public class DailyPnlDTO {

	@Schema(description = "일", example = "23")
	private Integer day;

	@Schema(description = "PnL (손익)", example = "150000.00")
	private BigDecimal pnl;

	@Schema(description = "피드백 요청 개수 (해당 날짜)", example = "3")
	private Integer feedbackCount;

	@Schema(description = "승률 (%)", example = "66.67")
	private Double winRate;
}
