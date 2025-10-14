package com.tradingpt.tpt_api.domain.investmenttypehistory.repository;

import static com.tradingpt.tpt_api.domain.investmenttypehistory.entity.QInvestmentTypeChangeRequest.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.investmenttypehistory.enums.ChangeRequestStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InvestmentTypeChangeRequestRepositoryImpl implements InvestmentTypeChangeRequestRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public boolean existsPendingRequestByCustomerId(Long customerId) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(investmentTypeChangeRequest)
			.where(
				investmentTypeChangeRequest.customer.id.eq(customerId),
				investmentTypeChangeRequest.status.eq(ChangeRequestStatus.PENDING)
			)
			.fetchFirst();

		return fetchOne != null;

	}
}
