package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

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
	 * 스윙 완강 후 전용 필드
	 */
	private LocalDate positionStartDate; // 포지션 진입 날짜
	private LocalDate positionEndDate; // 포지션 종료

	private Boolean directionFrameExists; // 디렉션 프레임 존재 유무
	private String directionFrame; // 디렉션 프레임
	private String mainFrame; // 메인 프레임
	private String subFrame; // 서브 프레임

	@Lob
	private String trendAnalysis; // 추세 분석

	@Lob
	private String trainerFeedbackRequestContent; // 담당 트레이너 피드백 요청 사항

	@Enumerated(EnumType.STRING)
	private EntryPoint entryPoint; // 진입 타점

	@Enumerated(EnumType.STRING)
	private Grade grade; // 등급

	private Integer additionalBuyCount; // 추가 매수 횟수
	private Integer splitSellCount; // 분할 매도 횟수

	/**
	 * DTO로부터 SwingRequestDetail 엔티티를 생성하는 정적 팩토리 메서드
	 *
	 * @param request DTO 요청 객체
	 * @param customer 피드백을 요청하는 고객
	 * @param title 피드백 요청 제목
	 * @return 생성된 SwingRequestDetail 엔티티
	 */
	public static SwingRequestDetail createFrom(
		CreateSwingRequestDetailRequestDTO request,
		Customer customer,
		String title) {

		// 1. 공통 필드 설정
		SwingRequestDetailBuilder<?, ?> builder = SwingRequestDetail.builder()
			.customer(customer)
			.title(title)
			.feedbackYear(request.getFeedbackYear())
			.feedbackMonth(request.getFeedbackMonth())
			.feedbackWeek(request.getFeedbackWeek())
			.feedbackRequestDate(request.getFeedbackRequestDate())
			.category(request.getCategory())
			.positionHoldingTime(request.getPositionHoldingTime())
			.position(request.getPosition())
			.courseStatus(request.getCourseStatus())
			.membershipLevel(request.getMembershipLevel())
			.riskTaking(request.getRiskTaking())
			.leverage(request.getLeverage())
			.pnl(request.getPnl())
			.totalAssetPnl(request.getTotalAssetPnl())
			.rnr(request.getRnr())
			.tradingReview(request.getTradingReview())
			.operatingFundsRatio(request.getOperatingFundsRatio())
			.entryPrice(request.getEntryPrice())
			.exitPrice(request.getExitPrice())
			.settingStopLoss(request.getSettingStopLoss())
			.settingTakeProfit(request.getSettingTakeProfit());

		// 2. 완강 여부에 따른 조건부 필드 설정
		if (request.getCourseStatus() == CourseStatus.BEFORE_COMPLETION) {
			// 완강 전 필드
			builder
				.positionStartReason(request.getPositionStartReason())
				.positionEndReason(request.getPositionEndReason());
		} else if (request.getCourseStatus() == CourseStatus.AFTER_COMPLETION) {
			// 완강 후 필드
			builder
				.positionStartDate(request.getPositionStartDate())
				.positionEndDate(request.getPositionEndDate())
				.directionFrameExists(request.getDirectionFrameExists())
				.directionFrame(request.getDirectionFrame())
				.mainFrame(request.getMainFrame())
				.subFrame(request.getSubFrame())
				.trendAnalysis(request.getTrendAnalysis())
				.trainerFeedbackRequestContent(request.getTrainerFeedbackRequestContent())
				.entryPoint(request.getEntryPoint())
				.grade(request.getGrade())
				.additionalBuyCount(request.getAdditionalBuyCount())
				.splitSellCount(request.getSplitSellCount());
		}

		// 3. 엔티티 생성 및 양방향 연관관계 설정
		SwingRequestDetail swingRequestDetail = builder.build();
		customer.getFeedbackRequests().add(swingRequestDetail);

		return swingRequestDetail;
	}

	@Override
	public InvestmentType getInvestmentType() {
		return InvestmentType.SWING;
	}
}
