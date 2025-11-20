package com.tradingpt.tpt_api.domain.weeklytradingsummary.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
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
	@JoinColumn(name = "trainer_id", nullable = true)  // 멤버십 미가입 고객은 트레이너가 없을 수 있음
	private Trainer trainer;

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
	 * @param trainer                 트레이너
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
		Trainer trainer,
		CourseStatus courseStatus,
		InvestmentType investmentType,
		Integer year,
		Integer month,
		Integer week
	) {
		WeeklyPeriod weeklyPeriod = WeeklyPeriod.of(year, month, week);

		return WeeklyTradingSummary.builder()
			.customer(customer)
			.trainer(trainer)
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

}
