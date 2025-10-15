package com.tradingpt.tpt_api.domain.weeklytradingsummary.repository;

import static com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.QWeeklyTradingSummary.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import lombok.RequiredArgsConstructor;

/**
 * WeeklyTradingSummary 커스텀 Repository 구현체
 */
@RequiredArgsConstructor
public class WeeklyTradingSummaryRepositoryImpl implements WeeklyTradingSummaryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public boolean existsByCustomerIdAndYearAndMonthAndWeekAndInvestmentType(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(weeklyTradingSummary)
			.where(
				weeklyTradingSummary.customer.id.eq(customerId),
				weeklyTradingSummary.period.year.eq(year),
				weeklyTradingSummary.period.month.eq(month),
				weeklyTradingSummary.period.week.eq(week),
				weeklyTradingSummary.investmentType.eq(investmentType)
			)
			.fetchFirst();

		return fetchOne != null;
	}
}