package com.tradingpt.tpt_api.domain.monthlytradingsummary.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;

public interface MonthlyTradingSummaryRepository
	extends JpaRepository<MonthlyTradingSummary, Long>, MonthlyTradingSummaryRepositoryCustom {

	Optional<MonthlyTradingSummary> findTopByEvaluator_IdAndCustomer_IdOrderByPeriodYearDescPeriodMonthDesc(
		Long evaluatorId,
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

	/**
	 * 특정 고객의 특정 연/월에 대한 월간 평가가 실제로 작성되었는지 확인
	 * (레코드 존재 여부가 아닌 monthlyEvaluation 필드가 채워졌는지 확인)
	 *
	 * 비즈니스 규칙:
	 * - 레코드가 존재해도 monthlyEvaluation이 null이면 미작성 상태
	 * - 트레이너가 monthlyEvaluation을 작성해야 완료된 것으로 간주
	 *
	 * @param customerId 고객 ID
	 * @param year       연도
	 * @param month      월
	 * @return monthlyEvaluation이 작성된 레코드 존재 여부
	 */
	boolean existsByCustomer_IdAndPeriod_YearAndPeriod_MonthAndMonthlyEvaluationIsNotNull(
		Long customerId,
		Integer year,
		Integer month
	);
}
