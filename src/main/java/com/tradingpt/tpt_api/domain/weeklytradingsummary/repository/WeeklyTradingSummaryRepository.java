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

	/**
	 * 특정 고객의 특정 연/월/주차에 대한 주간 평가 존재 여부 확인
	 * 평가 관리 화면에서 미작성 평가 판별에 사용
	 *
	 * @param customerId 고객 ID
	 * @param year       연도
	 * @param month      월
	 * @param week       주차
	 * @return 존재 여부
	 */
	boolean existsByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_Week(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);
}
