package com.tradingpt.tpt_api.domain.review.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.domain.review.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "review")
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
	private User user;

	@Builder.Default
	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReviewTagMapping> reviewTagMappings = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReviewAttachment> attachments = new ArrayList<>();

	/**
	 * 필드
	 */
	@Lob
	@Column(columnDefinition = "TEXT")
	private String content; // 후기 내용

	@Lob
	@Column(columnDefinition = "TEXT")
	private String replyContent; // 답변 내용

	@Builder.Default
	private LocalDateTime submittedAt = LocalDateTime.now(); // 후기 작성 일시

	private LocalDateTime repliedAt; // 답변 일시

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private Status status = Status.PUBLIC; // 공개 여부

	@Column(nullable = false)
	private Integer rating; // 별점 (1-5)

	/**
	 * 정적 팩토리 메서드
	 */
	public static Review createFrom(Customer customer, String processedContent, Integer rating) {
		return Review.builder()
			.customer(customer)
			.content(processedContent)
			.rating(rating)
			.build();
	}

	/**
	 * 사용자 편의 메서드
	 */
	public boolean hasReply() {
		return this.user != null && this.replyContent != null;
	}

	public boolean isPublic() {
		return this.status == Status.PUBLIC;
	}

	public void updateVisibility(Status status) {
		this.status = status;
	}

	public void addReply(User user, String content) {
		this.user = user;
		this.replyContent = content;
		this.repliedAt = LocalDateTime.now();
	}

	/**
	 * 태그 추가 메서드
	 */
	public void addTag(ReviewTag reviewTag) {
		ReviewTagMapping mapping = ReviewTagMapping.createFrom(this, reviewTag);
		this.reviewTagMappings.add(mapping);
	}

	public void addTags(List<ReviewTag> reviewTags) {
		reviewTags.forEach(this::addTag);
	}

	/**
	 * 첨부파일 추가 메서드
	 */
	public void addAttachment(String fileUrl, String fileKey) {
		ReviewAttachment attachment = ReviewAttachment.createFrom(this, fileUrl, fileKey);
		this.attachments.add(attachment);
	}

	public void addAttachments(List<String[]> fileInfos) {
		fileInfos.forEach(info -> addAttachment(info[0], info[1]));
	}

}
