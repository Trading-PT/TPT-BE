package com.tradingpt.tpt_api.domain.weeklytradingsummary.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;

public interface WeeklyTradingSummaryRepository extends JpaRepository<WeeklyTradingSummary, Long> {

	Optional<WeeklyTradingSummary> findTopByTrainer_IdAndCustomer_IdOrderByPeriodYearDescPeriodMonthDescPeriodWeekDesc(
		Long trainerId,
		Long customerId
	);
}
