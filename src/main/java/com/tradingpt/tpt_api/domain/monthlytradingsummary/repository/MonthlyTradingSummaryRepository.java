package com.tradingpt.tpt_api.domain.monthlytradingsummary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;

public interface MonthlyTradingSummaryRepository
	extends JpaRepository<MonthlyTradingSummary, Long>, MonthlyTradingSummaryRepositoryCustom {
	
}
