package com.tradingpt.tpt_api.domain.monthlytradingsummary.entity;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
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
	@JoinColumn(name = "trainer_id")
	private Trainer trainer;

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
	 * @param trainer 트레이너
	 * @param year 연도
	 * @param month 월
	 * @param investmentType 투자 타입 (반드시 DAY 또는 SWING)
	 * @return MonthlyTradingSummary 엔티티
	 */
	public static MonthlyTradingSummary createFromProcessed(
		String processedEvaluation,
		String processedGoal,
		Customer customer,
		Trainer trainer,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		MonthlyPeriod monthlyPeriod = MonthlyPeriod.of(year, month);

		return MonthlyTradingSummary.builder()
			.customer(customer)
			.trainer(trainer)
			.period(monthlyPeriod)
			.investmentType(investmentType)
			.monthlyEvaluation(processedEvaluation)
			.nextMonthGoal(processedGoal)
			.evaluatedAt(LocalDateTime.now())
			.build();
	}

	/**
	 * 평가 수정
	 */
	public void updateEvaluation(String monthlyEvaluation, String nextMonthGoal) {
		this.monthlyEvaluation = monthlyEvaluation;
		this.nextMonthGoal = nextMonthGoal;
		this.evaluatedAt = LocalDateTime.now();
	}

}
