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

	/** S3 파일 접근용 URL */
	private String fileUrl;

	/** S3 파일 삭제용 key */
	private String fileKey;

	public static FeedbackRequestAttachment createFrom(FeedbackRequest feedbackRequest, String fileUrl, String fileKey) {
		FeedbackRequestAttachment newScreenshot = FeedbackRequestAttachment.builder()
			.feedbackRequest(feedbackRequest)
			.fileUrl(fileUrl)
			.fileKey(fileKey)
			.build();

		feedbackRequest.getFeedbackRequestAttachments().add(newScreenshot);

		return newScreenshot;
	}

	/**
	 * 파일 정보 변경 (URL, Key 동시 변경)
	 */
	public void changeFile(String fileUrl, String fileKey) {
		this.fileUrl = fileUrl;
		this.fileKey = fileKey;
	}

}
