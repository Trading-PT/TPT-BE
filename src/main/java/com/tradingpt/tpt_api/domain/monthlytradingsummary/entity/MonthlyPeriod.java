package com.tradingpt.tpt_api.domain.monthlytradingsummary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월 단위 기간을 표현하는 값 객체.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode // 필드 값이 같으면 동등
public class MonthlyPeriod {

	@Column(name = "summary_year", nullable = false)
	private Integer year;

	@Column(name = "summary_month", nullable = false)
	private Integer month;

	public static MonthlyPeriod of(Integer year, Integer month) {
		return new MonthlyPeriod(year, month);
	}
}
