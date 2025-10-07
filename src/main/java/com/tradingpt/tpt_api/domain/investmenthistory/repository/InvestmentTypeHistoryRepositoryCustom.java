package com.tradingpt.tpt_api.domain.investmenthistory.repository;

import java.util.Optional;

import com.tradingpt.tpt_api.domain.investmenthistory.entity.InvestmentTypeHistory;

public interface InvestmentTypeHistoryRepositoryCustom {

	Optional<InvestmentTypeHistory> findActiveHistoryForMonth(Long customerId, Integer year, Integer month);

}
