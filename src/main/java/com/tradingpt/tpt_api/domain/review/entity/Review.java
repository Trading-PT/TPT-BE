package com.tradingpt.tpt_api.domain.review.entity;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.review.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "review_id")
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_id")
	private Trainer trainer;

	/**
	 * 필드
	 */
	@Lob
	@Column(columnDefinition = "TEXT")
	private String content; // 후기 내용

	@Lob
	@Column(columnDefinition = "TEXT")
	private String reply; // 답변 내용

	@Builder.Default
	private LocalDateTime submittedAt = LocalDateTime.now(); // 후기 작성 일시

	private LocalDateTime answeredAt; // 답변 일시

	@Builder.Default
	private Status status = Status.PRIVATE; // 공개 여부

	@Builder.Default
	private Boolean isAnswered = Boolean.FALSE; // 답변 작성 여부

}
