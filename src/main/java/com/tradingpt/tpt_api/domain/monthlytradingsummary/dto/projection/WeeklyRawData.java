package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 주차별 원시 통계 데이터 (Repository 조회 전용)
 */
@Getter
@AllArgsConstructor
public class WeeklyRawData {
	private Integer week;
	private Integer tradingCount;
	private BigDecimal weeklyPnl;
	private Integer winCount;  // 승률 계산용
	private BigDecimal totalRiskTaking;  // R&R 계산용
}