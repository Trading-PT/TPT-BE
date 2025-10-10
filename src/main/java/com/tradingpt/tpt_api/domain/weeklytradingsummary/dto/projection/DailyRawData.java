package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 일별 원시 통계 데이터 (Repository 조회 전용)
 */
@Getter
@AllArgsConstructor
public class DailyRawData {
	private LocalDate date;           // 날짜
	private Integer tradingCount;     // 매매 횟수
	private BigDecimal dailyPnl;      // 일간 P&L
	private Integer winCount;         // 승리 횟수
	private BigDecimal totalRiskTaking; // 총 리스크 테이킹
	private Integer nCount;           // Status.N 개수
	private Integer fnCount;          // Status.FN 개수
}