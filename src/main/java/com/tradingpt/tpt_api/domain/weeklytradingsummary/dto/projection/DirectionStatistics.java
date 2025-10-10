package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 방향성 통계 (데이 트레이딩 전용)
 */
@Getter
@AllArgsConstructor
public class DirectionStatistics {
	// 방향성 O
	private Integer directionOCount;
	private Double directionOWinRate;
	private Double directionORnr;

	// 방향성 X
	private Integer directionXCount;
	private Double directionXWinRate;
	private Double directionXRnr;
}