package com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyPnlProjection {
	private LocalDate feedbackRequestDate; // 날짜
	private BigDecimal totalPnl; // 총 pnl 합
	private Long winCount; // PNL > 0인 매매 개수 (승리)
	private Long feedbackCount; // 피드백 총 개수 (전체 매매)
}
