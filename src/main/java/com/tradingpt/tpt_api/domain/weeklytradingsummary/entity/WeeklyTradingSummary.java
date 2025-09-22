package com.tradingpt.tpt_api.domain.weeklytradingsummary.entity;

import java.math.BigDecimal;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
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
	@JoinColumn(name = "trainer_id", nullable = false)
	private Trainer trainer;

	/**
	 * 필드
	 */
	@Enumerated(EnumType.STRING)
	private InvestmentType investmentType; // 투자 유형 (DAY 전용)

	@Embedded
	private WeeklyPeriod period; // 요약 연/월/주

	private Integer tradingCount; // 매매 횟수

	private BigDecimal weeklyPnl; // 주간 P&L

	@Lob
	private String weeklyEvaluation; // 주간 회원 매매평가

	@Lob
	private String weeklyProfitableTradingAnalysis; // 수익난 메메 분석

	@Lob
	private String weeklyLossTradingAnalysis; // 손실난 매매분석

}
