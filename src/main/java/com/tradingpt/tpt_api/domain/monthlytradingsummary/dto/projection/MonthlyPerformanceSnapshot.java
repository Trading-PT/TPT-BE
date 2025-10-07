package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 월별 성과 스냅샷 (Repository 조회 전용)
 */
@Getter
@AllArgsConstructor
public class MonthlyPerformanceSnapshot {
	private BigDecimal finalWinRate;
	private BigDecimal averageRnr;
	private BigDecimal finalPnl;
}