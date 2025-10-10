package com.tradingpt.tpt_api.domain.weeklytradingsummary.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;

public interface WeeklyTradingSummaryRepository
	extends JpaRepository<WeeklyTradingSummary, Long>, WeeklyTradingSummaryRepositoryCustom {

	Optional<WeeklyTradingSummary> findTopByTrainer_IdAndCustomer_IdOrderByPeriodYearDescPeriodMonthDescPeriodWeekDesc(
		Long trainerId,
		Long customerId
	);

	Optional<WeeklyTradingSummary> findByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_Week(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);
	
}
