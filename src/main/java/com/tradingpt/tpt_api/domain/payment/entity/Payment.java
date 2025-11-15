package com.tradingpt.tpt_api.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.domain.payment.enums.PaymentStatus;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentType;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
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
@Table(name = "payment")
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_id")
	private Subscription subscription;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_method_id")
	private PaymentMethod paymentMethod;

	/**
	 * 필드
	 */
	@Column(unique = true)
	private String orderId; // 주문번호 (merchant_uid)

	private String orderName; // 주문명 (예: 2025년 1월 구독료)

	private BigDecimal amount; // 결제 금액

	private BigDecimal vat; // 부가세
	private BigDecimal discountAmount; // 할인 금액

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private PaymentStatus status = PaymentStatus.PENDING; // 결제 상태

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private PaymentType paymentType = PaymentType.RECURRING; // 구독 상태

	@Column(unique = true)
	private String paymentKey; // PG 결제 키 (NicePay payment key)

	private String pgTid; // PG 거래 고유번호 (transaction id)

	private String pgAuthCode; // PG 승인 번호

	private String pgResponseCode; // PG 응답 코드

	@Lob
	@Column(columnDefinition = "TEXT")
	private String pgResponseMessage; // PG 응답 메시지

	@Column(columnDefinition = "json")
	private String pgMetadata; // PG사 응답 원본 데이터

	@Builder.Default
	private LocalDateTime requestedAt = LocalDateTime.now(); // 결제 요청 일시

	private LocalDateTime paidAt; // 결제 완료 일시

	private LocalDateTime failedAt; // 결제 실패 일시

	private LocalDateTime cancelledAt; // 취소 일시

	private LocalDateTime refundedAt; // 환불 일시

	@Lob
	@Column(columnDefinition = "TEXT")
	private String failureReason; // 결제 실패 사유

	private String failureCode; // 실패 코드

	private BigDecimal refundAmount; // 환불 금액

	@Lob
	@Column(columnDefinition = "TEXT")
	private String refundReason; // 환불 사유

	private String pgRefundTid; // PG 환불 거래번호

	@Lob
	@Column(columnDefinition = "TEXT")
	private String cancelReason; // 취소 사유

	private String receiptUrl; // 영수증 URL

	private LocalDate billingPeriodStart; // 청구 기간 시작일

	private LocalDate billingPeriodEnd; // 청구 기간 종료일

	@Column(columnDefinition = "json")
	private String paymentMethodSnapshot; // 결제 당시 결제수단 정보 스냅샷

	@Builder.Default
	private Boolean isPromotional = Boolean.FALSE; // 프로모션 결제 여부

	private String promotionDetail; // 프로모션 상세 (예: 사전등록 2개월 무료)
}
