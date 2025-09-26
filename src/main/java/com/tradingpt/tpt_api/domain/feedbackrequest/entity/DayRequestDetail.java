package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
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
@Table(name = "day_request_detail")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DiscriminatorValue(value = "DAY")
public class DayRequestDetail extends FeedbackRequest {

	/**
	 * 필드
	 */
	private String category; // 종목

	private Integer riskTaking; // 리스크 테이킹

	private Integer leverage; // 레버리지

	@Enumerated(EnumType.STRING)
	private Position position; // 포지션

	@Lob
	private String trainerFeedbackRequestContent; // 담당 트레이너 피드백 요청 사항

	private Boolean directionFrameExists; // 디렉션 프레임 방향성 유무

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

	@Lob
	private String tradingReview; // 매매 복기

	public static DayRequestDetail createFrom(CreateDayRequestDetailRequestDTO request, Customer customer,
		FeedbackPeriodUtil.FeedbackPeriod period, String title) {
		PreCourseFeedbackDetail preCourseFeedbackDetail = request.toPreCourseFeedbackDetail();

		DayRequestDetail newDayRequestDetail = DayRequestDetail.builder()
			.customer(customer)
			.title(title)
			.feedbackYear(period.year())
			.feedbackMonth(period.month())
			.feedbackWeek(period.week())
			.feedbackRequestedAt(request.getRequestDate())
			.positionHoldingTime(request.getPositionHoldingTime())
			.courseStatus(request.getCourseStatus())
			.preCourseFeedbackDetail(preCourseFeedbackDetail)
			.category(request.getCategory())
			.positionHoldingTime(request.getPositionHoldingTime())
			.riskTaking(request.getRiskTaking())
			.leverage(request.getLeverage())
			.position(request.getPosition())
			.trainerFeedbackRequestContent(request.getTrainerFeedbackRequestContent())
			.directionFrame(request.getDirectionFrame())
			.mainFrame(request.getMainFrame())
			.subFrame(request.getSubFrame())
			.directionFrameExists(request.getDirectionFrameExists())
			.trendAnalysis(request.getTrendAnalysis())
			.pnl(request.getPnl())
			.winLossRatio(request.getWinLossRatio())
			.entryPoint1(request.getEntryPoint1())
			.grade(request.getGrade())
			.entryPoint2(request.getEntryPoint2())
			.tradingReview(request.getTradingReview())
			.build();

		customer.getFeedbackRequests().add(newDayRequestDetail); // 양방향 연관 관계 매핑

		return newDayRequestDetail;
	}

	@Override
	public FeedbackType getFeedbackType() {
		return FeedbackType.DAY;
	}

}
