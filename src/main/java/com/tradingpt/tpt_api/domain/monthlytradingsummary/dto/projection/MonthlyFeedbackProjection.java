package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyFeedbackProjection {
	private Integer month;
	private Long totalCount;
	private Integer nCount;   // Status.N 개수
	private Integer fnCount;  // Status.FN 개수
}
