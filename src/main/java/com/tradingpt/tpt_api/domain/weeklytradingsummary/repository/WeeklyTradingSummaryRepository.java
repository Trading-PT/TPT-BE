package com.tradingpt.tpt_api.domain.weeklytradingsummary.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;

public interface WeeklyTradingSummaryRepository
	extends JpaRepository<WeeklyTradingSummary, Long>, WeeklyTradingSummaryRepositoryCustom {

	Optional<WeeklyTradingSummary> findTopByEvaluator_IdAndCustomer_IdOrderByPeriodYearDescPeriodMonthDescPeriodWeekDesc(
		Long evaluatorId,
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

	/**
	 * 특정 고객의 특정 연/월/주차에 대한 주간 평가가 실제로 작성되었는지 확인
	 * (레코드 존재 여부가 아닌 weeklyEvaluation 필드가 채워졌는지 확인)
	 *
	 * 비즈니스 규칙:
	 * - 고객이 BEFORE_COMPLETION 때 memo만 작성한 레코드가 있어도
	 * - 트레이너가 weeklyEvaluation을 작성하지 않았으면 pending 목록에 표시해야 함
	 *
	 * @param customerId 고객 ID
	 * @param year       연도
	 * @param month      월
	 * @param week       주차
	 * @return weeklyEvaluation이 작성된 레코드 존재 여부
	 */
	boolean existsByCustomer_IdAndPeriod_YearAndPeriod_MonthAndPeriod_WeekAndWeeklyEvaluationIsNotNull(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	);
}
