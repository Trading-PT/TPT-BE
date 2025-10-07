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
	private Integer reverseWinCount;
	private Double reverseRnr;

	private Integer pullBackCount;
	private Integer pullBackWinCount;
	private Double pullBackRnr;

	private Integer breakOutCount;
	private Integer breakOutWinCount;
	private Double breakOutRnr;
}