package com.tradingpt.tpt_api.domain.weeklytradingsummary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주 단위 기간을 표현하는 값 객체.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode // 필드 값이 같으면 동등
public class WeeklyPeriod {

	@Column(name = "summary_year", nullable = false)
	private Integer year;

	@Column(name = "summary_month", nullable = false)
	private Integer month;

	@Column(name = "summary_week", nullable = false)
	private Integer week;

	public static WeeklyPeriod of(Integer year, Integer month, Integer week) {
		return new WeeklyPeriod(year, month, week);
	}
}
