package com.tradingpt.tpt_api.domain.investmenthistory.entity;

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
	private LocalDate startedAt;

	@Column(name = "ended_at")
	private LocalDate endedAt;

	public void assignCustomer(Customer customer) {
		this.customer = customer;
	}

	public void closeAt(LocalDate endDate) {
		if (endDate == null) {
			return;
		}
		if (startedAt != null && endDate.isBefore(startedAt)) {
			this.endedAt = startedAt;
			return;
		}
		this.endedAt = endDate;
	}

	public boolean isActiveOn(LocalDate targetDate) {
		if (targetDate == null) {
			return false;
		}
		boolean startsBeforeOrEquals = startedAt == null || !startedAt.isAfter(targetDate);
		boolean endsAfterOrEquals = endedAt == null || !endedAt.isBefore(targetDate);
		return startsBeforeOrEquals && endsAfterOrEquals;
	}

	public boolean isOngoing() {
		return endedAt == null;
	}
}
