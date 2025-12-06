package com.tradingpt.tpt_api.domain.weeklytradingsummary.entity;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.exception.WeeklyTradingSummaryErrorStatus;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.exception.WeeklyTradingSummaryException;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@SuperBuilder
@DynamicUpdate
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "weekly_trading_summary")
public class WeeklyTradingSummary extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "weekly_trading_summary_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "evaluator_id", nullable = true)  // 평가 작성자 (ADMIN 또는 TRAINER, nullable)
	private User evaluator;

	/**
	 * 필드
	 */

	/**
	 * 주별 매매 일지는 BEFORE_COMPLETION일 경우 memo만 작성 가능하지만
	 * AFTER_COMPLETION에 경우
	 * DAY 투자 유형일 때만
	 * weeklyEvaluation, weeklyProfitableTradingAnalysis, weeklyLossTradingAnalysis
	 * 를 작성할 수 있다.
	 */
	@Enumerated(EnumType.STRING)
	private CourseStatus courseStatus;

	@Enumerated(EnumType.STRING)
	private InvestmentType investmentType; // 투자 유형

	@Embedded
	private WeeklyPeriod period; // 요약 연/월/주

	@Lob
	@Column(columnDefinition = "TEXT")
	private String memo; // 나의 문제점 메모하기

	@Lob
	@Column(columnDefinition = "TEXT")
	private String weeklyEvaluation; // 주간 회원 매매평가

	@Lob
	@Column(columnDefinition = "TEXT")
	private String weeklyProfitableTradingAnalysis; // 수익난 매매 분석

	@Lob
	@Column(columnDefinition = "TEXT")
	private String weeklyLossTradingAnalysis; // 손실난 매매분석

	@Builder.Default
	private LocalDateTime evaluatedAt = LocalDateTime.now(); // 트레이너 평가

	/**
	 * 정적 팩토리 메서드: 처리된 콘텐츠로 WeeklyTradingSummary 생성
	 *
	 * @param processedMemo           처리된 메모
	 * @param processedEvaluation     처리된 주간 평가 (nullable)
	 * @param processedProfitAnalysis 처리된 수익 분석 (nullable)
	 * @param processedLossAnalysis   처리된 손실 분석 (nullable)
	 * @param customer                고객
	 * @param evaluator               평가 작성자 (ADMIN 또는 TRAINER, nullable)
	 * @param courseStatus            코스 상태
	 * @param investmentType          투자 타입
	 * @param year                    연도
	 * @param month                   월
	 * @param week                    주
	 * @return WeeklyTradingSummary 엔티티
	 */
	public static WeeklyTradingSummary createFromProcessed(
		String processedMemo,
		String processedEvaluation,
		String processedProfitAnalysis,
		String processedLossAnalysis,
		Customer customer,
		User evaluator,
		CourseStatus courseStatus,
		InvestmentType investmentType,
		Integer year,
		Integer month,
		Integer week
	) {
		WeeklyPeriod weeklyPeriod = WeeklyPeriod.of(year, month, week);

		return WeeklyTradingSummary.builder()
			.customer(customer)
			.evaluator(evaluator)
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.period(weeklyPeriod)
			.memo(processedMemo)
			.weeklyEvaluation(processedEvaluation)
			.weeklyProfitableTradingAnalysis(processedProfitAnalysis)
			.weeklyLossTradingAnalysis(processedLossAnalysis)
			.evaluatedAt(LocalDateTime.now())
			.build();
	}

	/**
	 * 주간 요약 수정
	 */
	public void updateSummary(
		String memo,
		String weeklyEvaluation,
		String weeklyProfitableTradingAnalysis,
		String weeklyLossTradingAnalysis
	) {
		this.memo = memo;
		this.weeklyEvaluation = weeklyEvaluation;
		this.weeklyProfitableTradingAnalysis = weeklyProfitableTradingAnalysis;
		this.weeklyLossTradingAnalysis = weeklyLossTradingAnalysis;
		this.evaluatedAt = LocalDateTime.now();
	}

	// ========================================
	// DDD 비즈니스 규칙 메서드 (Tell, Don't Ask)
	// ========================================

	/**
	 * 고객이 메모를 작성/수정할 수 있는지 확인
	 * 비즈니스 규칙: BEFORE_COMPLETION 그룹일 때만 고객이 메모 작성 가능
	 *
	 * @return 고객이 메모를 작성할 수 있으면 true
	 */
	public boolean canCustomerWriteMemo() {
		return this.courseStatus != null && this.courseStatus.isBeforeCompletionGroup();
	}

	/**
	 * 트레이너가 평가를 작성/수정할 수 있는지 확인
	 * 비즈니스 규칙: AFTER_COMPLETION + DAY 타입일 때만 트레이너가 평가 작성 가능
	 *
	 * @return 트레이너가 평가를 작성할 수 있으면 true
	 */
	public boolean canTrainerWriteEvaluation() {
		return this.courseStatus == CourseStatus.AFTER_COMPLETION
			&& this.investmentType == InvestmentType.DAY;
	}

	/**
	 * SWING 타입에서 주간 평가가 없는지 확인
	 * 비즈니스 규칙: SWING 타입은 완강 후에도 주간 평가가 없음
	 *
	 * @return SWING 타입이면서 완강 후이면 true
	 */
	public boolean hasNoWeeklyEvaluationForSwing() {
		return this.courseStatus == CourseStatus.AFTER_COMPLETION
			&& this.investmentType == InvestmentType.SWING;
	}

	// ========================================
	// DDD 비즈니스 메서드 (상태 변경 캡슐화)
	// ========================================

	/**
	 * 고객 메모 업데이트 (비즈니스 규칙 검증 포함)
	 * JPA Dirty Checking을 활용하여 변경 사항 자동 반영
	 *
	 * @param processedMemo 처리된(sanitized) 메모 내용
	 * @throws WeeklyTradingSummaryException 완강 후에는 메모 수정 불가
	 */
	public void updateCustomerMemo(String processedMemo) {
		if (!canCustomerWriteMemo()) {
			throw new WeeklyTradingSummaryException(
				WeeklyTradingSummaryErrorStatus.CUSTOMER_CANNOT_CREATE_FOR_AFTER_COMPLETION
			);
		}
		this.memo = processedMemo;
	}

	/**
	 * 트레이너 평가 업데이트 (PREMIUM 멤버십 전용)
	 * JPA Dirty Checking을 활용하여 변경 사항 자동 반영
	 *
	 * 비즈니스 규칙:
	 * - MembershipLevel 검증은 Service에서 수행 (PREMIUM만 허용)
	 * - CourseStatus 검증 제거됨 (기존 레코드가 BEFORE_COMPLETION이어도 PREMIUM이면 평가 가능)
	 *
	 * @param processedEvaluation     처리된 주간 평가
	 * @param processedProfitAnalysis 처리된 수익 매매 분석
	 * @param processedLossAnalysis   처리된 손실 매매 분석
	 */
	public void updateTrainerEvaluation(
		String processedEvaluation,
		String processedProfitAnalysis,
		String processedLossAnalysis
	) {
		// MembershipLevel 검증은 Service에서 수행
		// CourseStatus와 무관하게 PREMIUM이면 평가 가능
		this.weeklyEvaluation = processedEvaluation;
		this.weeklyProfitableTradingAnalysis = processedProfitAnalysis;
		this.weeklyLossTradingAnalysis = processedLossAnalysis;
		this.evaluatedAt = LocalDateTime.now();
	}

	// ========================================
	// 정적 팩토리 메서드 (DDD 패턴)
	// ========================================

	/**
	 * 고객 메모용 주간 요약 생성 (BEFORE_COMPLETION)
	 *
	 * @param processedMemo  처리된 메모 내용
	 * @param customer       고객
	 * @param evaluator      평가 작성자 (nullable, 고객 메모 생성 시에는 보통 null)
	 * @param investmentType 투자 타입
	 * @param year           연도
	 * @param month          월
	 * @param week           주
	 * @return 새로운 WeeklyTradingSummary 엔티티
	 */
	public static WeeklyTradingSummary createForCustomerMemo(
		String processedMemo,
		Customer customer,
		User evaluator,
		InvestmentType investmentType,
		Integer year,
		Integer month,
		Integer week
	) {
		return WeeklyTradingSummary.builder()
			.customer(customer)
			.evaluator(evaluator)
			.courseStatus(CourseStatus.BEFORE_COMPLETION)
			.investmentType(investmentType)
			.period(WeeklyPeriod.of(year, month, week))
			.memo(processedMemo)
			.build();
	}

	/**
	 * 평가 작성용 주간 요약 생성 (PREMIUM 멤버십 전용)
	 * ADMIN 또는 TRAINER가 평가를 작성할 때 사용
	 *
	 * @param processedEvaluation     처리된 주간 평가
	 * @param processedProfitAnalysis 처리된 수익 매매 분석
	 * @param processedLossAnalysis   처리된 손실 매매 분석
	 * @param customer                고객
	 * @param evaluator               평가 작성자 (ADMIN 또는 TRAINER)
	 * @param courseStatus            고객의 실제 완강 상태 (FeedbackRequest 또는 Customer에서 가져옴)
	 * @param investmentType          투자 타입
	 * @param year                    연도
	 * @param month                   월
	 * @param week                    주
	 * @return 새로운 WeeklyTradingSummary 엔티티
	 */
	public static WeeklyTradingSummary createForEvaluation(
		String processedEvaluation,
		String processedProfitAnalysis,
		String processedLossAnalysis,
		Customer customer,
		User evaluator,
		CourseStatus courseStatus,
		InvestmentType investmentType,
		Integer year,
		Integer month,
		Integer week
	) {
		return WeeklyTradingSummary.builder()
			.customer(customer)
			.evaluator(evaluator)
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.period(WeeklyPeriod.of(year, month, week))
			.weeklyEvaluation(processedEvaluation)
			.weeklyProfitableTradingAnalysis(processedProfitAnalysis)
			.weeklyLossTradingAnalysis(processedLossAnalysis)
			.evaluatedAt(LocalDateTime.now())
			.build();
	}

}
