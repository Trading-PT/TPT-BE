package com.tradingpt.tpt_api.domain.investmenttypehistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory;

public interface InvestmentTypeHistoryRepository
	extends JpaRepository<InvestmentTypeHistory, Long>, InvestmentTypeHistoryRepositoryCustom {

}
