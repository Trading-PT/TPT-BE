package com.tradingpt.tpt_api.domain.feedbackresponse.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
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
@Table(name = "feedback_response")
public class FeedbackResponse extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_response_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feedback_request_id")
	private FeedbackRequest feedbackRequest;

	@Builder.Default
	@OneToMany(mappedBy = "feedbackResponse", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FeedbackResponseAttachment> feedbackResponseAttachments = new ArrayList<>();

	/**
	 * 필드
	 */

	@Lob
	private String content; // 피드백 내용

	private LocalDateTime submittedAt; // 피드백 제공 시각

}
