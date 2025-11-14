package com.tradingpt.tpt_api.domain.paymentmethod.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.domain.paymentmethod.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
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
@DynamicInsert
@DynamicUpdate
@Table(name = "billing_request")
public class BillingRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "billing_request_id")
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
	@Column(nullable = false, unique = true)
	private String moid; // 가맹점에서 부여한 주문번호 (Unique하게 구성)

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Status status = Status.PENDING; // 빌링키 등록 요청 완료 여부

	private String resultCode; // 결과 코드

	@Lob
	@Column(columnDefinition = "TEXT")
	private String resultMsg; // 결과 메시지

	private LocalDateTime completedAt; // 빌링키 완료 시각

	/**
	 * 팩토리 생성 메서드
	 */
	public static BillingRequest of(String moid, Customer customer) {
		return BillingRequest.builder()
			.moid(moid)
			.customer(customer)
			.status(Status.PENDING)
			.build();
	}

	/**
	 * 편의 메서드
	 */
	public void complete() {
		this.status = Status.COMPLETED;
		this.completedAt = LocalDateTime.now();
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
}
