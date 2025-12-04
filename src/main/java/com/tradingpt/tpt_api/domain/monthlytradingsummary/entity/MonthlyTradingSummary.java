package com.tradingpt.tpt_api.domain.monthlytradingsummary.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.exception.MonthlyTradingSummaryErrorStatus;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.exception.MonthlyTradingSummaryException;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "monthly_trading_summary")
public class MonthlyTradingSummary extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "monthly_trading_summary_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "evaluator_id", nullable = true)
	private User evaluator;

	/**
	 * 필드
	 */
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private CourseStatus courseStatus = CourseStatus.AFTER_COMPLETION; // 월별 매매 일지의 리뷰는 완강 후만 존재한다.

	@Embedded
	private MonthlyPeriod period; // 요약 연/월

	@Enumerated(EnumType.STRING)
	private InvestmentType investmentType;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String monthlyEvaluation; // 한달 회원 매매 평가

	@Lob
	@Column(columnDefinition = "TEXT")
	private String nextMonthGoal; // 다음달 회원 목표

	@Builder.Default
	private LocalDateTime evaluatedAt = LocalDateTime.now(); // 트레이너 평가 시각

	/**
	 * 정적 팩토리 메서드: 처리된 콘텐츠로 MonthlyTradingSummary 생성
	 *
	 * @param processedEvaluation 처리된 평가 내용
	 * @param processedGoal 처리된 목표 내용
	 * @param customer 고객
	 * @param evaluator 평가 작성자 (ADMIN 또는 TRAINER)
	 * @param year 연도
	 * @param month 월
	 * @param investmentType 투자 타입 (반드시 DAY 또는 SWING)
	 * @return MonthlyTradingSummary 엔티티
	 */
	public static MonthlyTradingSummary createFromProcessed(
		String processedEvaluation,
		String processedGoal,
		Customer customer,
		User evaluator,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		MonthlyPeriod monthlyPeriod = MonthlyPeriod.of(year, month);

		return MonthlyTradingSummary.builder()
			.customer(customer)
			.evaluator(evaluator)
			.period(monthlyPeriod)
			.investmentType(investmentType)
			.monthlyEvaluation(processedEvaluation)
			.nextMonthGoal(processedGoal)
			.evaluatedAt(LocalDateTime.now())
			.build();
	}

	/**
	 * 평가 작성용 월간 요약 생성 (AFTER_COMPLETION)
	 * ADMIN 또는 TRAINER가 평가를 작성할 때 사용
	 *
	 * @param processedEvaluation 처리된 월간 평가
	 * @param processedGoal       처리된 다음 달 목표
	 * @param customer            고객
	 * @param evaluator           평가 작성자 (ADMIN 또는 TRAINER)
	 * @param investmentType      투자 타입
	 * @param year                연도
	 * @param month               월
	 * @return 새로운 MonthlyTradingSummary 엔티티
	 */
	public static MonthlyTradingSummary createForEvaluation(
		String processedEvaluation,
		String processedGoal,
		Customer customer,
		User evaluator,
		InvestmentType investmentType,
		Integer year,
		Integer month
	) {
		return MonthlyTradingSummary.builder()
			.customer(customer)
			.evaluator(evaluator)
			.courseStatus(CourseStatus.AFTER_COMPLETION)
			.investmentType(investmentType)
			.period(MonthlyPeriod.of(year, month))
			.monthlyEvaluation(processedEvaluation)
			.nextMonthGoal(processedGoal)
			.evaluatedAt(LocalDateTime.now())
			.build();
	}

	// ========================================
	// DDD 비즈니스 규칙 메서드 (Tell, Don't Ask)
	// ========================================

	/**
	 * 평가 수정 (레거시 - 검증 없음)
	 */
	public void updateEvaluation(String monthlyEvaluation, String nextMonthGoal) {
		this.monthlyEvaluation = monthlyEvaluation;
		this.nextMonthGoal = nextMonthGoal;
		this.evaluatedAt = LocalDateTime.now();
	}

	// ========================================
	// DDD 비즈니스 메서드 (상태 변경 캡슐화)
	// ========================================

	/**
	 * 트레이너가 평가를 작성/수정할 수 있는지 확인
	 * 비즈니스 규칙: AFTER_COMPLETION일 때만 트레이너가 월간 평가 작성 가능 (DAY/SWING 모두)
	 *
	 * @return 트레이너가 평가를 작성할 수 있으면 true
	 */
	public boolean canTrainerWriteEvaluation() {
		return this.courseStatus == CourseStatus.AFTER_COMPLETION;
	}

	// ========================================
	// 정적 팩토리 메서드 (DDD 패턴)
	// ========================================

	/**
	 * 트레이너 평가 업데이트 (비즈니스 규칙 검증 포함)
	 * JPA Dirty Checking을 활용하여 변경 사항 자동 반영
	 *
	 * @param processedEvaluation 처리된 월간 평가
	 * @param processedGoal       처리된 다음 달 목표
	 * @throws MonthlyTradingSummaryException 완강 전이면 평가 수정 불가
	 */
	public void updateTrainerEvaluation(String processedEvaluation, String processedGoal) {
		if (!canTrainerWriteEvaluation()) {
			throw new MonthlyTradingSummaryException(
				MonthlyTradingSummaryErrorStatus.COURSE_NOT_COMPLETED
			);
		}
		this.monthlyEvaluation = processedEvaluation;
		this.nextMonthGoal = processedGoal;
		this.evaluatedAt = LocalDateTime.now();
	}

}
