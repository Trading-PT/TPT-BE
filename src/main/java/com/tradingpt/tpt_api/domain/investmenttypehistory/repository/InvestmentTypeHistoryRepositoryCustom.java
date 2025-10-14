package com.tradingpt.tpt_api.domain.investmenttypehistory.repository;

import java.util.Optional;

import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory;

public interface InvestmentTypeHistoryRepositoryCustom {

	Optional<InvestmentTypeHistory> findActiveHistoryForMonth(Long customerId, Integer year, Integer month);

}
