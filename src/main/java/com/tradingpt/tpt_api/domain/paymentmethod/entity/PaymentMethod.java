package com.tradingpt.tpt_api.domain.paymentmethod.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.domain.paymentmethod.enums.CardType;
import com.tradingpt.tpt_api.domain.paymentmethod.enums.PaymentMethodType;
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
import jakarta.persistence.OneToOne;
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
@Table(name = "payment_method")
public class PaymentMethod extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_method_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "billing_request")
	private BillingRequest billingRequest;

	/**
	 * 필드
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private PaymentMethodType paymentMethodType = PaymentMethodType.CARD; // 결제 타입

	private String orderId; // 주문 아이디

	private String pgCustomerKey; // PG사 고객 식별키

	@Column(unique = true)
	private String billingKey; // 정기 결제용 빌링키

	private LocalDateTime billingKeyIssuedAt; // 빌링키 발급 일시

	private String displayName; // 화면 표시명 (예: 신한 **** 1234, 카카오페이)

	private String maskedIdentifier; // 마스킹된 카드번호/계좌번호

	private String cardCompanyCode; // 카드사 코드 (예: 신한, 국민, 삼성)

	private String cardCompanyName; // 카드사명 (예: 신한카드)

	@Enumerated(EnumType.STRING)
	private CardType cardType;

	@Column(columnDefinition = "json")
	private String simplePayMetadata; // 카카오페이/네이버페이 추가 정보

	@Builder.Default
	private Boolean isActive = Boolean.TRUE; // 사용 가능 여부

	@Builder.Default
	private Boolean isPrimary = Boolean.TRUE; // 주 결제수단 여부

	@Builder.Default
	private Boolean isDeleted = Boolean.FALSE; // 삭제 여부

	private LocalDate expiresAt; // 만료일 (카드의 경우 YY/MM)

	private String pgResponseCode; // PG 응답 코드

	@Lob
	@Column(columnDefinition = "TEXT")
	private String pgResponseMessage; // PG 응답 메시지

	@Column(columnDefinition = "json")
	private String pgMetadata; // PG사 추가 응답 데이터

	private LocalDateTime lastFailedAt; // 마지막 결제 실패 일시

	@Builder.Default
	private Integer failureCount = 0; // 연속 실패 횟수

	private LocalDateTime deletedAt; // 삭제 일시

	/**
	 * 팩토리 생성 메서드
	 */
	public static PaymentMethod of(Customer customer, String orderId, String billingKey,
		LocalDateTime billingKeyIssuedAt, String cardCompanyCode, String cardCompanyName,
		CardType cardtype, String maskedIdentifier, String displayName, String pgResponseCode,
		String pgResponseMessage
	) {
		return PaymentMethod.builder()
			.customer(customer)
			.orderId(orderId)
			.billingKey(billingKey)
			.billingKeyIssuedAt(billingKeyIssuedAt)
			.cardCompanyCode(cardCompanyCode)
			.cardCompanyName(cardCompanyName)
			.cardType(cardtype)
			.maskedIdentifier(maskedIdentifier)
			.displayName(displayName)
			.pgResponseCode(pgResponseCode)
			.pgResponseMessage(pgResponseMessage)
			.build();

	}

	public boolean isActive() {
		return isActive != null && isActive;
	}

	public boolean isExpired() {
		return expiresAt != null && expiresAt.isBefore(LocalDate.now());
	}

	public void delete() {
		this.isActive = Boolean.FALSE;
		this.isPrimary = Boolean.FALSE;
		this.isDeleted = Boolean.TRUE;
		this.deletedAt = LocalDateTime.now();
	}

	public void setPgResponseCode(String pgResponseCode, String pgResponseMessage) {
		this.pgResponseCode = pgResponseCode;
		this.pgResponseMessage = pgResponseMessage;
	}
}
