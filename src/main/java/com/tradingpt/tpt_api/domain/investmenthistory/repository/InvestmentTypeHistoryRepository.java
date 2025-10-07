package com.tradingpt.tpt_api.domain.investmenthistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.investmenthistory.entity.InvestmentTypeHistory;

public interface InvestmentTypeHistoryRepository
	extends JpaRepository<InvestmentTypeHistory, Long>, InvestmentTypeHistoryRepositoryCustom {
	
}
