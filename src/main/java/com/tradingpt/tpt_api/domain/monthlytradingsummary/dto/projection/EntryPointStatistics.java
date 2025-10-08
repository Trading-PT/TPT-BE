package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 진입 타점별 통계 (Repository 조회 전용)
 */
@Getter
@AllArgsConstructor
public class EntryPointStatistics {
	private Integer reverseCount;
	private Double reverseWinRate;
	private Double reverseRnr;

	private Integer pullBackCount;
	private Double pullBackWinRate;
	private Double pullBackRnr;

	private Integer breakOutCount;
	private Double breakOutWinRate;
	private Double breakOutRnr;
}