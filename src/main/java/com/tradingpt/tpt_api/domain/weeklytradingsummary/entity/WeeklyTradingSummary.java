package com.tradingpt.tpt_api.domain.weeklytradingsummary.entity;

import java.math.BigDecimal;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	@JoinColumn(name = "monthly_trading_summary_id")
	private MonthlyTradingSummary monthlyTradingSummary;

	/**
	 * 필드
	 */
	private Integer summaryYear; // 요약 연도

	private Integer summaryMonth; // 요약 월

	private Integer summaryWeek; // 요약 주차

	private Integer tradingCount; // 매매 횟수

	private BigDecimal weeklyPnl; // 주간 P&L

	@Lob
	private String weeklyEvaluation; // 주간 회원 매매평가

	@Lob
	private String weeklyProfitableTradingAnalysis; // 수익난 메메 분석

	@Lob
	private String weeklyLossTradingAnalysis; // 손실난 매매분석

}
