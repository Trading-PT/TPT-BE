package com.tradingpt.tpt_api.domain.monthlytradingsummary.repository;

import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

/**
 * Monthly Trading Summary 커스텀 Repository 인터페이스
 * QueryDSL 사용한 동적 쿼리 메서드 정의
 */
public interface MonthlyTradingSummaryRepositoryCustom {

	/**
	 * 해당 고객의 특정 연/월/투자타입에 대한 월간 요약이 이미 존재하는지 확인
	 *
	 * @param customerId     고객 ID
	 * @param year           연도
	 * @param month          월
	 * @param investmentType 투자 타입
	 * @return 존재하면 true, 없으면 false
	 */
	boolean existsByCustomerIdAndYearAndMonthAndInvestmentType(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	);
}
