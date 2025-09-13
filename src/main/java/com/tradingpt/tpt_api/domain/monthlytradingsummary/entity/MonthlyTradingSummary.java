package com.tradingpt.tpt_api.domain.monthlytradingsummary.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
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
	@Builder.Default
	@OneToMany(mappedBy = "monthlyTradingSummary", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<WeeklyTradingSummary> weeklyTradingSummaries = new ArrayList<>();

	/**
	 * 필드
	 */
	private Integer summary_year; // 요약 연도

	private Integer summary_month; // 요약 월

	@Enumerated(EnumType.STRING)
	private InvestmentType investmentType;

	private Integer monthlyWinRate; // 월간 최종 승률

	private Integer monthlyAvgRatio; // 월간 평균 손익비

	private BigDecimal monthlyFinalPnl; // 월간 최종 P&L

	@Lob
	private String monthlyEvaluation; // 한달 회원 매매 평가

	@Lob
	private String nextMonthGoal; // 다음달 회원 목표

}
