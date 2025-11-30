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

	/**
	 * 특정 고객의 특정 연/월에 대한 월간 평가 존재 여부 확인
	 * 평가 관리 화면에서 미작성 평가 판별에 사용
	 *
	 * @param customerId 고객 ID
	 * @param year       연도
	 * @param month      월
	 * @return 존재 여부
	 */
	boolean existsByCustomer_IdAndPeriod_YearAndPeriod_Month(
		Long customerId,
		Integer year,
		Integer month
	);
}
