package com.tradingpt.tpt_api.domain.feedbackresponse.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.user.entity.User;
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
import jakarta.persistence.ManyToOne;
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

	/**
	 * 피드백 응답 작성자 (Trainer 또는 Admin)
	 * Admin도 피드백 응답을 작성할 수 있도록 User 타입으로 정의
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_id")
	private User writer;

	@Builder.Default
	@OneToMany(mappedBy = "feedbackResponse", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FeedbackResponseAttachment> feedbackResponseAttachments = new ArrayList<>();

	/**
	 * 필드
	 */
	private String title; // 피드백 제목

	@Lob
	@Column(columnDefinition = "TEXT")
	private String content; // 피드백 내용

	@Builder.Default
	private LocalDateTime submittedAt = LocalDateTime.now(); // 피드백 제공 시각

	public static FeedbackResponse createFrom(FeedbackRequest feedbackRequest, User writer,
		String title, String responseContent) {
		FeedbackResponse newFeedbackResponse = FeedbackResponse.builder()
			.feedbackRequest(feedbackRequest)
			.writer(writer)
			.title(title)
			.content(responseContent)
			.build();

		feedbackRequest.setFeedbackResponse(newFeedbackResponse);

		return newFeedbackResponse;
	}

	/**
	 * 작성자 조회 (하위 호환성을 위한 메서드)
	 * @deprecated Use getWriter() instead
	 */
	@Deprecated
	public User getTrainer() {
		return this.writer;
	}

	public void updateContent(String newContent) {
		this.content = newContent;
	}

	/**
	 * 첨부파일 추가
	 */
	public void addAttachment(String fileUrl, String fileKey) {
		FeedbackResponseAttachment attachment = FeedbackResponseAttachment.createFrom(this, fileUrl, fileKey);
		this.feedbackResponseAttachments.add(attachment);
	}

	/**
	 * 기존 첨부파일 전체 삭제 (수정 시 사용)
	 */
	public void clearAttachments() {
		this.feedbackResponseAttachments.clear();
	}

}
