package com.tradingpt.tpt_api.domain.monthlytradingsummary.repository;

import static com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.QMonthlyTradingSummary.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import lombok.RequiredArgsConstructor;

/**
 * MonthlyTradingSummary 커스텀 Repository 구현체
 */
@RequiredArgsConstructor
public class MonthlyTradingSummaryRepositoryImpl implements MonthlyTradingSummaryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public boolean existsByCustomerIdAndYearAndMonthAndInvestmentType(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(monthlyTradingSummary)
			.where(
				monthlyTradingSummary.customer.id.eq(customerId),
				monthlyTradingSummary.period.year.eq(year),
				monthlyTradingSummary.period.month.eq(month),
				monthlyTradingSummary.investmentType.eq(investmentType)
			)
			.fetchFirst();

		return fetchOne != null;
	}
}