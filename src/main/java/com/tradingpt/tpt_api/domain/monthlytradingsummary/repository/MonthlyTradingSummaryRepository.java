package com.tradingpt.tpt_api.domain.monthlytradingsummary.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;

public interface MonthlyTradingSummaryRepository
	extends JpaRepository<MonthlyTradingSummary, Long>, MonthlyTradingSummaryRepositoryCustom {

	Optional<MonthlyTradingSummary> findTopByTrainer_IdAndCustomer_IdOrderByPeriodYearDescPeriodMonthDesc(
		Long trainerId,
		Long customerId
	);

	Optional<MonthlyTradingSummary> findByCustomer_IdAndPeriod_YearAndPeriod_Month(
		Long customerId,
		Integer year,
		Integer month
	);
}
