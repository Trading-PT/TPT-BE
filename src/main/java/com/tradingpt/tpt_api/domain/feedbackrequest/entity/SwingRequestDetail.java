package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.user.entity.Customer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "swing_request_detail")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DiscriminatorValue(value = "SWING")
public class SwingRequestDetail extends FeedbackRequest {

	/**
	 * 필드
	 */
	private String category; // 종목

	private LocalDate positionStartDate; // 포지션 진입 날짜

	private LocalDate positionEndDate; // 포지션 종료 날짜

	private Integer riskTaking; // 리스크 테이킹

	private Integer leverage; // 레버리지

	@Enumerated(EnumType.STRING)
	private Position position; // 숏, 롱

	@Lob
	private String trainerFeedbackRequestContent; // 담당 트레이너 피드백 요청 사항

	private String directionFrame; // 디렉션 프레임

	private String mainFrame; // 메인 프레임

	private String subFrame; // 서브 프레임

	@Lob
	private String trendAnalysis; // 추세 분석

	private BigDecimal pnl; // P&L

	private String winLossRatio; // 손익비

	@Enumerated(EnumType.STRING)
	private EntryPoint entryPoint1; // 1 진입 타점

	@Enumerated(EnumType.STRING)
	private Grade grade; // 등급

	private LocalDateTime entryPoint2; // 2 진입 타점

	private LocalDateTime entryPoint3; // 3 진입 타점

	@Lob
	private String tradingReview; // 매매 복기

	public static SwingRequestDetail createFrom(CreateSwingRequestDetailRequestDTO request, Customer customer,
		String title) {
		PreCourseFeedbackDetail preCourseFeedbackDetail = request.toPreCourseFeedbackDetail();

		return SwingRequestDetail.builder()
			.customer(customer)
			.title(title)
			.feedbackYear(request.getFeedbackYear())
			.feedbackMonth(request.getFeedbackMonth())
			.feedbackWeek(request.getFeedbackWeek())
			.feedbackRequestedAt(request.getRequestDate())
			.positionHoldingTime(request.getPositionHoldingTime())
			.courseStatus(request.getCourseStatus())
			.membershipLevel(request.getMembershipLevel())
			.preCourseFeedbackDetail(preCourseFeedbackDetail)
			.category(request.getCategory())
			.positionStartDate(request.getPositionStartDate())
			.positionEndDate(request.getPositionEndDate())
			.riskTaking(request.getRiskTaking())
			.leverage(request.getLeverage())
			.position(request.getPosition())
			.trainerFeedbackRequestContent(request.getTrainerFeedbackRequestContent())
			.directionFrame(request.getDirectionFrame())
			.mainFrame(request.getMainFrame())
			.subFrame(request.getSubFrame())
			.trendAnalysis(request.getTrendAnalysis())
			.pnl(request.getPnl())
			.winLossRatio(request.getWinLossRatio())
			.entryPoint1(request.getEntryPoint1())
			.grade(request.getGrade())
			.entryPoint2(request.getEntryPoint2())
			.entryPoint3(request.getEntryPoint3())
			.tradingReview(request.getTradingReview())
			.build();
	}

	@Override
	public FeedbackType getFeedbackType() {
		return FeedbackType.SWING;
	}
}
