package com.tradingpt.tpt_api.domain.weeklytradingsummary.repository;

import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

/**
 * WeeklyTradingSummary 커스텀 Repository 인터페이스
 */
public interface WeeklyTradingSummaryRepositoryCustom {

	/**
	 * 해당 고객의 특정 연/월/주/투자타입에 대한 주간 요약이 이미 존재하는지 확인
	 *
	 * @param customerId     고객 ID
	 * @param year           연도
	 * @param month          월
	 * @param week           주
	 * @param investmentType 투자 타입
	 * @return 존재하면 true, 없으면 false
	 */
	boolean existsByCustomerIdAndYearAndMonthAndWeekAndInvestmentType(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	);
}