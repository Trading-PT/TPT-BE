package com.tradingpt.tpt_api.domain.consultation.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "consultation")
public class Consultation extends BaseEntity {

	/**
	 * 유저와 연관관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "consultation_id")
	private Long id;

	private LocalDate consultationDate;

	private LocalTime consultationTime;

	@Builder.Default
	private String memo = null; // 기본 빈 문자열

	@Builder.Default
	private Boolean isProcessed = false; // 기본값 false

	public void accept() {
		this.isProcessed = true;
	}

	public void changeMemo(String memo) {
		this.memo = (memo == null ? "" : memo);
	}
}
