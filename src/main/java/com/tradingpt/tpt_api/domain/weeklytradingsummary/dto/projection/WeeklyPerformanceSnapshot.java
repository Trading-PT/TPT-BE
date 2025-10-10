package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 주간 성과 스냅샷
 */
@Getter
@AllArgsConstructor
public class WeeklyPerformanceSnapshot {
	private Double finalWinRate;      // 최종 승률
	private Double averageRnr;        // 평균 R&R
	private BigDecimal finalPnl;          // 최종 P&L
}