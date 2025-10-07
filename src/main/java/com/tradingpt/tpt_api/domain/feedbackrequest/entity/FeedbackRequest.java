package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feedback_request")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ftype", discriminatorType = DiscriminatorType.STRING)
public abstract class FeedbackRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_request_id")
	private Long id;

	/**
	 * 연관 관계 매핑
	 */
	// ⭐ 부모에서 Customer와 연관 관계 정의
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@Builder.Default
	@OneToMany(mappedBy = "feedbackRequest", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FeedbackRequestAttachment> feedbackRequestAttachments = new ArrayList<>();

	@OneToOne(mappedBy = "feedbackRequest", cascade = CascadeType.ALL, orphanRemoval = true)
	private FeedbackResponse feedbackResponse;

	/**
	 * 필드
	 */
	private String title;

	private LocalDate feedbackRequestedAt; // 피드백 요청 일자

	private String positionHoldingTime; // 포지션 홀딩 시간

	private String category; // 종목

	private Integer riskTaking; // 리스크 테이킹

	private Integer leverage; // 레버리지

	@Enumerated(EnumType.STRING)
	private CourseStatus courseStatus; // 완강 여부

	@Enumerated(EnumType.STRING)
	private MembershipLevel membershipLevel;

	@Embedded
	private PreCourseFeedbackDetail preCourseFeedbackDetail;

	private BigDecimal pnl; // P&L

	private Double rnr; // R&R

	private Integer feedbackYear; // 피드백 연도

	private Integer feedbackMonth; // 피드백 월

	private Integer feedbackWeek; // 피드백 주차

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private Status status = Status.N; // 피드백 답변 여부

	@Lob
	private String tradingReview; // 매매 복기

	@Builder.Default
	private Boolean isRead = false;

	@Builder.Default
	private Boolean isResponded = false;

	@Builder.Default
	private Boolean isBestFeedback = false; // 베스트 피드백 여부

	// 추상 메서드로 FeedbackType 반환
	public abstract FeedbackType getFeedbackType();

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setFeedbackResponse(FeedbackResponse feedbackResponse) {
		this.feedbackResponse = feedbackResponse;
	}

	public String getPositionStartReason() {
		return preCourseFeedbackDetail != null ? preCourseFeedbackDetail.getPositionStartReason() : null;
	}

	public String getPositionEndReason() {
		return preCourseFeedbackDetail != null ? preCourseFeedbackDetail.getPositionEndReason() : null;
	}

}
