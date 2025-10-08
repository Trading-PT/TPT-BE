package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.user.entity.Customer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "scalping_request_detail")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue(value = "SCALPING")
public class ScalpingRequestDetail extends FeedbackRequest {

	/**
	 * DTO로부터 ScalpingRequestDetail 엔티티를 생성하는 정적 팩토리 메서드
	 * 스캘핑은 완강 전/후 모두 operatingFundsRatio 등의 필드를 사용한다.
	 *
	 * @param request DTO 요청 객체
	 * @param customer 피드백을 요청하는 고객
	 * @param period 피드백 기간 정보 (년, 월, 주차)
	 * @param title 피드백 요청 제목
	 * @return 생성된 ScalpingRequestDetail 엔티티
	 */
	public static ScalpingRequestDetail createFrom(
		CreateScalpingRequestDetailRequestDTO request,
		Customer customer,
		FeedbackPeriodUtil.FeedbackPeriod period,
		String title) {

		ScalpingRequestDetail scalpingRequestDetail = ScalpingRequestDetail.builder()
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
			.rnr(request.getRnr())
			.tradingReview(request.getTradingReview())
			.operatingFundsRatio(request.getOperatingFundsRatio())
			.entryPrice(request.getEntryPrice())
			.exitPrice(request.getExitPrice())
			.settingStopLoss(request.getSettingStopLoss())
			.settingTakeProfit(request.getSettingTakeProfit())
			.positionStartReason(request.getPositionStartReason())
			.positionEndReason(request.getPositionEndReason())
			.build();

		// 4. 엔티티 생성 및 양방향 연관관계 설정
		customer.getFeedbackRequests().add(scalpingRequestDetail);

		return scalpingRequestDetail;
	}

	// ⭐ getFeedbackType() 구현
	@Override
	public FeedbackType getFeedbackType() {
		return FeedbackType.SCALPING;
	}
}
