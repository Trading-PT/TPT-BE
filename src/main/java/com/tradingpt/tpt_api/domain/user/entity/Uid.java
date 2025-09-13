package com.tradingpt.tpt_api.domain.user.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
	uniqueConstraints = {
		// 같은 고객이 같은 UID를 중복 등록하는 것을 방지
		@UniqueConstraint(name = "uk_customer_uid", columnNames = {"customer_id", "uid"})
	}
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Uid extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String exchangeName;  // 거래소명 (필요 시 Enum로 승격 가능)

	@Column(nullable = false)
	private String uid;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", referencedColumnName = "user_id", nullable = false)
	private Customer customer;


	// ⭐ Customer 설정을 위한 패키지 접근 메서드 (편의 메서드용)
	void assignCustomer(Customer customer) {
		this.customer = customer;
	}

}
