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
	private Double averagePnlPercentage; // 평균 pnl
	private Long feedbackCount; // 피드백 총 개수
}
