package com.tradingpt.tpt_api.domain.weeklytradingsummary.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WeeklyTradingSummaryRepositoryImpl implements WeeklyTradingSummaryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

}
