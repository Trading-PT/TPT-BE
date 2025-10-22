package com.tradingpt.tpt_api.domain.complaint.entity;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.complaint.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "complaint")
public class Complaint extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "answered_id")
	private Trainer answeredBy; // 답변 작성자

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "complaint_id")
	private Long id;

	@Column(name = "title")
	private String title;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status status = Status.UNANSWERED;

	@Builder.Default
	@Column(name = "complaint_reply", columnDefinition = "TEXT")
	private String complaintReply = null;

	@Builder.Default
	@Column(name = "answered_at")
	private LocalDateTime answeredAt = null;

	/** 답변 등록/수정: 연관된 트레이너/답변 내용/시간/상태를 일괄 갱신 */
	public void upsertReply(Trainer answeredBy, String reply, LocalDateTime answeredTime) {
		this.answeredBy = answeredBy;
		this.complaintReply = reply;
		this.answeredAt = answeredTime;
		this.status = Status.ANSWERED;
	}

	/** 답변 삭제: 답변/작성자/시간 초기화 + 미답변 상태로 변경 */
	public void deleteReply(LocalDateTime now) {
		this.complaintReply = null;
		this.answeredBy = null;
		this.answeredAt = null;
		this.status = Status.UNANSWERED;
	}
}
