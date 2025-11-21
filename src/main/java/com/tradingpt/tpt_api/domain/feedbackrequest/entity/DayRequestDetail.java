package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import jakarta.persistence.Column;
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
	 * 데이 완강 후 전용 필드
	 */
	private Boolean directionFrameExists;
	private String directionFrame;
	private String mainFrame;
	private String subFrame;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String trendAnalysis; // 추세 분석

	@Lob
	@Column(columnDefinition = "TEXT")
	private String trainerFeedbackRequestContent; // 담당 트레이너 피드백 요청 사항

	@Enumerated(EnumType.STRING)
	private EntryPoint entryPoint; // 진입 타점

	@Enumerated(EnumType.STRING)
	private Grade grade; // 등급

	private Integer additionalBuyCount; // 추가 매수 횟수
	private Integer splitSellCount; // 분할 매도 횟수

	/**
	 * DTO로부터 DayRequestDetail 엔티티를 생성하는 정적 팩토리 메서드
	 *
	 * @param request DTO 요청 객체
	 * @param customer 피드백을 요청하는 고객
	 * @param period 피드백 기간 정보 (년, 월, 주차)
	 * @param title 피드백 요청 제목
	 * @return 생성된 DayRequestDetail 엔티티
	 */
	public static DayRequestDetail createFrom(
		CreateDayRequestDetailRequestDTO request,
		Customer customer,
		FeedbackPeriodUtil.FeedbackPeriod period,
		String title) {

		// 1. 공통 필드 설정 (완강 전/후 무관)
		DayRequestDetailBuilder<?, ?> builder = DayRequestDetail.builder()
			.customer(customer)
			.title(title)
			.feedbackYear(period.year())
			.feedbackMonth(period.month())
			.feedbackWeek(period.week())
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
				.directionFrameExists(request.getDirectionFrameExists())
				.directionFrame(request.getDirectionFrame())
				.mainFrame(request.getMainFrame())
				.subFrame(request.getSubFrame())
				.trendAnalysis(request.getTrendAnalysis())
				.entryPoint(request.getEntryPoint())
				.grade(request.getGrade())
				.additionalBuyCount(request.getAdditionalBuyCount())
				.splitSellCount(request.getSplitSellCount())
				.trainerFeedbackRequestContent(request.getTrainerFeedbackRequestContent());
		}

		// 3. 엔티티 생성
		DayRequestDetail dayRequestDetail = builder.build();

		// 4. 양방향 연관관계 설정
		customer.getFeedbackRequests().add(dayRequestDetail);

		return dayRequestDetail;
	}

	@Override
	public InvestmentType getInvestmentType() {
		return InvestmentType.DAY;
	}

}
