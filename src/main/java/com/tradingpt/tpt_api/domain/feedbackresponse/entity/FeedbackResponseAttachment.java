package com.tradingpt.tpt_api.domain.feedbackresponse.entity;

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
@Table(name = "feedback_response_attachment")
public class FeedbackResponseAttachment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_response_attachment_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feedback_response_id")
	private FeedbackResponse feedbackResponse;

	private String fileUrl; // 파일 url
}
