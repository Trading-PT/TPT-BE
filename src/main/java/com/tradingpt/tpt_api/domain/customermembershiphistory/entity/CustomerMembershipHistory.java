package com.tradingpt.tpt_api.domain.customermembershiphistory.entity;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.customermembershiphistory.enums.MembershipStatus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
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
@Table(name = "customer_membership_history")
public class CustomerMembershipHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_membership_history_id")
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
	private MembershipLevel membershipLevel; // 멤버쉽 단계

	private MembershipStatus membershipStatus;

	private String reasonCode;

	@Lob
	private String reasonDetail;

	private LocalDateTime validFrom;

	private LocalDateTime validTo;
}
