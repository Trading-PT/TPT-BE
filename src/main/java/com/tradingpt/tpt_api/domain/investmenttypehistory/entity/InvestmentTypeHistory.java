package com.tradingpt.tpt_api.domain.investmenttypehistory.entity;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "investment_history")
public class InvestmentTypeHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "investment_history_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	/**
	 * 필드
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "investment_type", nullable = false)
	private InvestmentType investmentType;

	@Column(name = "started_at", nullable = false)
	private LocalDate startDate;

	@Column(name = "ended_at")
	private LocalDate endDate;

	/**
	 * 정적 팩토리 메서드: 새로운 투자 타입 이력 생성
	 */
	public static InvestmentTypeHistory createFrom(
		Customer customer,
		InvestmentType investmentType,
		LocalDate startDate
	) {
		InvestmentTypeHistory history = InvestmentTypeHistory.builder()
			.customer(customer)
			.investmentType(investmentType)
			.startDate(startDate)
			.endDate(null)  // 진행 중
			.build();

		customer.getInvestmentHistories().add(history);

		return history;
	}

	public void assignCustomer(Customer customer) {
		this.customer = customer;
	}

	public void closeAt(LocalDate endDate) {
		if (endDate == null) {
			return;
		}
		if (startDate != null && endDate.isBefore(startDate)) {
			this.endDate = startDate;
			return;
		}
		this.endDate = endDate;
	}

	public boolean isActiveOn(LocalDate targetDate) {
		if (targetDate == null) {
			return false;
		}
		boolean startsBeforeOrEquals = startDate == null || !startDate.isAfter(targetDate);
		boolean endsAfterOrEquals = endDate == null || !endDate.isBefore(targetDate);
		return startsBeforeOrEquals && endsAfterOrEquals;
	}

	public boolean isOngoing() {
		return endDate == null;
	}
}
