package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.math.BigDecimal;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
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
@Table(name = "scalping_request_detail")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DiscriminatorValue(value = "SCALPING")
public class ScalpingRequestDetail extends FeedbackRequest {

	/**
	 * 필드
	 */
	private String category; // 종목

	private Integer riskTaking; // 리스크 테이킹

	private Integer leverage; // 레버리지

	@Enumerated(EnumType.STRING)
	private Position position; // 포지션

	private BigDecimal pnl; // P&L

	private Double rnr; // R&R

	private Integer operatingFundsRatio; // 비중 (운용 자금 대비)

	private BigDecimal entryPrice; // 진입 자금

	private BigDecimal exitPrice; // 탈출 자금

	private BigDecimal settingStopLoss; // 설정 손절가

	private BigDecimal settingTakeProfit; // 설정 익절가

	@Lob
	private String positionStartReason; // 포지션 진입 근거

	@Lob
	private String positionEndReason; // 포지션 탈출 근거

	@Lob
	private String tradingReview; // 매매 복기

	public static ScalpingRequestDetail createFrom(CreateScalpingRequestDetailRequestDTO request, Customer customer,
		FeedbackPeriodUtil.FeedbackPeriod period, String title) {
		PreCourseFeedbackDetail preCourseFeedbackDetail = request.toPreCourseFeedbackDetail();

		return ScalpingRequestDetail.builder()
			.customer(customer)
			.title(title)
			.feedbackRequestedAt(request.getRequestDate())
			.positionHoldingTime(request.getPositionHoldingTime())
			.courseStatus(request.getCourseStatus())
			.membershipLevel(request.getMembershipLevel())
			.preCourseFeedbackDetail(preCourseFeedbackDetail)
			.feedbackYear(period.year())
			.feedbackMonth(period.month())
			.feedbackWeek(period.week())
			.category(request.getCategory())
			.riskTaking(request.getRiskTaking())
			.leverage(request.getLeverage())
			.position(request.getPosition())
			.pnl(request.getPnl())
			.rnr(request.getRnr())
			.operatingFundsRatio(request.getOperatingFundsRatio())
			.entryPrice(request.getEntryPrice())
			.exitPrice(request.getExitPrice())
			.settingStopLoss(request.getSettingStopLoss())
			.settingTakeProfit(request.getSettingTakeProfit())
			.positionStartReason(request.getPositionStartReason())
			.positionEndReason(request.getPositionEndReason())
			.tradingReview(request.getTradingReview())
			.build();
	}

	// ⭐ getFeedbackType() 구현
	@Override
	public FeedbackType getFeedbackType() {
		return FeedbackType.SCALPING;
	}
}
