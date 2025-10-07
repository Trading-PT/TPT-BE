package com.tradingpt.tpt_api.domain.investmenthistory.repository;

import static com.tradingpt.tpt_api.domain.investmenthistory.entity.QInvestmentTypeHistory.*;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.investmenthistory.entity.InvestmentTypeHistory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InvestmentTypeHistoryRepositoryImpl implements InvestmentTypeHistoryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<InvestmentTypeHistory> findActiveHistoryForMonth(Long customerId, Integer year, Integer month) {
		// 해당 월의 첫날과 마지막날
		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

		InvestmentTypeHistory result = queryFactory
			.selectFrom(investmentTypeHistory)
			.where(
				investmentTypeHistory.customer.id.eq(customerId),
				investmentTypeHistory.startedAt.loe(endOfMonth),  // startDate <= 월 마지막날
				investmentTypeHistory.endedAt.isNull()
					.or(investmentTypeHistory.endedAt.goe(startOfMonth))  // endDate >= 월 첫날 OR endDate IS NULL
			)
			.orderBy(investmentTypeHistory.startedAt.desc())
			.fetchFirst();

		return Optional.ofNullable(result);
	}
}
