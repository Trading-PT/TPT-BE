package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
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
@AllArgsConstructor
@Table(name = "feedback_request")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "investment_type", discriminatorType = DiscriminatorType.STRING)
public abstract class FeedbackRequest extends BaseEntity {

	/**
	 * 베스트 피드백 최대 개수
	 * Admin이 선택할 수 있는 베스트 피드백의 최대 개수
	 */
	public static final int MAX_BEST_FEEDBACK_COUNT = 4;

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
	 * 완강 전/후 모두 공통인 필드만 (진짜 공통)
	 */
	private String title;
	private LocalDate feedbackRequestDate; // 피드백 요청 일자
	private String category; // 종목
	private String positionHoldingTime; // 포지션 홀딩 시간

	private BigDecimal riskTaking; // 리스크 테이킹
	private BigDecimal leverage; // 레버리지

	@Enumerated(EnumType.STRING)
	private Position position; // 포지션

	@Enumerated(EnumType.STRING)
	private CourseStatus courseStatus; // 완강 여부

	@Enumerated(EnumType.STRING)
	private MembershipLevel membershipLevel;

	private BigDecimal pnl; // P&L
	private BigDecimal totalAssetPnl; // 전체 자산 기준 P&L (pnl * operatingFundsRatio / 100)
	private Double rnr; // R&R

	private Integer feedbackYear; // 피드백 연도
	private Integer feedbackMonth; // 피드백 월
	private Integer feedbackWeek; // 피드백 주차

	/**
	 * ⭐ 매매 관련 필드
	 * - operatingFundsRatio: 완강 전 모든 타입 + 스캘핑 완강 후 필수
	 * - entryPrice, exitPrice, settingStopLoss: 모든 타입 완강 전/후 필수
	 * - settingTakeProfit: 모든 타입 선택 (필수 아님)
	 */
	private Integer operatingFundsRatio; // 비중 (운용 자금 대비)
	private BigDecimal entryPrice; // 진입 가격
	private BigDecimal exitPrice; // 탈출 가격
	private BigDecimal settingStopLoss; // 설정 손절가
	private BigDecimal settingTakeProfit; // 설정 익절가

	@Lob
	@Column(columnDefinition = "TEXT")
	private String positionStartReason; // 포지션 진입 근거

	@Lob
	@Column(columnDefinition = "TEXT")
	private String positionEndReason; // 포지션 탈출 근거

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private Status status = Status.N; // 피드백 답변 여부

	@Lob
	@Column(columnDefinition = "TEXT")
	private String tradingReview; // 매매 복기

	@Builder.Default
	private Boolean isRead = false; // 피드백 답변 읽은 여부

	@Builder.Default
	private Boolean isResponded = false; // 피드백 답변 여부

	@Builder.Default
	private Boolean isBestFeedback = false; // 베스트 피드백 여부

	@Builder.Default
	private Boolean isTokenUsed = false; // 토큰을 사용한 피드백인지

	private Integer tokenAmount; // 사용한 토큰 개수

	@Builder.Default
	private Boolean isTrainerWritten = Boolean.FALSE; // 트레이너 작성 여부

	// 추상 메서드로 FeedbackType 반환
	public abstract InvestmentType getInvestmentType();

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setFeedbackResponse(FeedbackResponse feedbackResponse) {
		this.feedbackResponse = feedbackResponse;
	}

	public void updateIsBestFeedback(boolean b) {
		this.isBestFeedback = b;
	}

	public void useToken(Integer tokenAmount) {
		this.isTokenUsed = true;
		this.tokenAmount = tokenAmount;
	}
}
