package com.tradingpt.tpt_api.domain.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.enums.SubscriptionType;
import com.tradingpt.tpt_api.domain.subscriptionplan.entity.SubscriptionPlan;
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
@Table(name = "subscription")
public class Subscription extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subscription_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_plan_id")
	private SubscriptionPlan subscriptionPlan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_method_id")
	private PaymentMethod paymentMethod;

	/**
	 * 필드
	 */
	private BigDecimal subscribedPrice; // 구독 시작 시 확정된 금액 (중복 저장)

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Status status = Status.ACTIVE; // 구독 상태

	private LocalDate currentPeriodStart; // 현재 결제 주기 시작일

	private LocalDate currentPeriodEnd; // 현재 결제 주기 종료일

	private LocalDate nextBillingDate; // 다음 결제 예정일

	private LocalDate lastBillingDate; // 마지막 결제 성공일

	private LocalDateTime cancelledAt; // 해지 일시

	@Lob
	@Column(columnDefinition = "TEXT")
	private String cancellationReason; // 해지 사유

	@Builder.Default
	private Integer paymentFailedCount = 0; // 연속 결제 실패 횟수

	private LocalDateTime lastPaymentFailedAt; // 마지막 결제 실패 일시

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private SubscriptionType subscriptionType = SubscriptionType.REGULAR; // 구독 타입 (프로모션, 일반)
	
	private String promotionNote; // ex) 프로모션 메모 (예: 2개월 무료 사전등록)

	// 이번 구독 기간 시작 시점에 이미 열려 있던 강의 개수 snapshot
	@Column(name = "base_opened_lecture_count", nullable = false)
	private int baseOpenedLectureCount;
}
