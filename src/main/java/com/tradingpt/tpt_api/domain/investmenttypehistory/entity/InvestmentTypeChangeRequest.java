package com.tradingpt.tpt_api.domain.investmenttypehistory.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.investmenttypehistory.enums.ChangeRequestStatus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
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
@Table(name = "investment_type_change_request")
public class InvestmentTypeChangeRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "investment_type_change_request_id")
	private Long id;

	/**
	 * 연관 관계
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_id")
	private Trainer trainer;

	/**
	 * 필드
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InvestmentType currentType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InvestmentType requestedType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ChangeRequestStatus status = ChangeRequestStatus.PENDING;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String reason; // 변경 사유

	@Column(nullable = false)
	private LocalDate requestedDate; // 신청일

	@Column(nullable = false)
	private LocalDate targetChangeDate; // 변경 예정일 (다음 달 1일)

	private LocalDateTime processedAt; // 승인/거부 처리 시각

	@Lob
	@Column(columnDefinition = "TEXT")
	private String rejectionReason; // 거부 사유

	/**
	 * 정적 팩토리 메서드: 변경 신청 생성
	 */
	public static InvestmentTypeChangeRequest createRequest(
		Customer customer,
		InvestmentType currentType,
		InvestmentType requestedType,
		String reason,
		LocalDate targetChangeDate
	) {
		return InvestmentTypeChangeRequest.builder()
			.customer(customer)
			.currentType(currentType)
			.requestedType(requestedType)
			.reason(reason)
			.requestedDate(LocalDate.now())
			.targetChangeDate(targetChangeDate)
			.status(ChangeRequestStatus.PENDING)
			.build();
	}

	/**
	 * 승인/거부 가능 여부 확인
	 */
	public boolean canBeProcessed() {
		return this.status == ChangeRequestStatus.PENDING;
	}

	/**
	 * 신청 승인
	 */
	public void approve(Trainer trainer) {
		this.status = ChangeRequestStatus.APPROVED;
		this.trainer = trainer;
		this.processedAt = LocalDateTime.now();
	}

	public void reject(Trainer trainer, String rejectionReason) {
		this.trainer = trainer;
		this.rejectionReason = rejectionReason;
	}

	/**
	 * 신청 취소 (고객)
	 */
	public void cancel() {
		this.status = ChangeRequestStatus.CANCELLED;
		this.processedAt = LocalDateTime.now();
	}
	
}
