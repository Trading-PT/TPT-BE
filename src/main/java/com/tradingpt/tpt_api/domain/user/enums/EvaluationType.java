package com.tradingpt.tpt_api.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 평가 유형
 *
 * - WEEKLY: 주간 평가 (완강 후 DAY 타입만)
 * - MONTHLY: 월간 평가 (완강 후 DAY/SWING 모두)
 */
@Getter
@AllArgsConstructor
public enum EvaluationType {

	WEEKLY("주간 평가"),
	MONTHLY("월간 평가")
	;

	private final String description;
}
