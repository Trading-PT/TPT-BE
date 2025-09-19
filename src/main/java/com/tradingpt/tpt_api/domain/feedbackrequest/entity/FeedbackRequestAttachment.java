package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

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
@Table(name = "feedback_request_attachment")
public class FeedbackRequestAttachment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_reqeust_attachment_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feedback_reuqest_id")
	private FeedbackRequest feedbackRequest;

	private String fileUrl; // 이미지 파일 url

	public static FeedbackRequestAttachment createFrom(FeedbackRequest feedbackRequest, String fileUrl) {
		FeedbackRequestAttachment newScreenshot = FeedbackRequestAttachment.builder()
			.feedbackRequest(feedbackRequest)
			.fileUrl(fileUrl)
			.build();

		feedbackRequest.getFeedbackRequestAttachments().add(newScreenshot);

		return newScreenshot;
	}

}
