package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.Customer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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
	private LocalDate requestDate; // 날짜

	private String category; // 종목

	private Integer dailyTradingCount; // 하루 매매 횟수

	private Integer riskTaking; // 리스크 테이킹

	private Integer leverage; // 레버리지

	private Integer totalPositionTakingCount; // 총 포지션 잡은 횟수

	private Integer totalProfitMarginPerTrades; // 총 매매 횟수 대비 수익 매매횟수

	@Lob
	private String positionStartReason; // 스켈핑 시 포지션을 진입하는 근거

	@Lob
	private String positionEndReason; // 스켈핑 시 포지션을 종료하는 근거

	@Lob
	private String trainerFeedbackRequestContent; // 담당 트레이너 피드백 요청 사항

	@Lob
	private String trendAnalysis; // 15분봉 기준 추세 분석

	public static ScalpingRequestDetail createFrom(CreateScalpingRequestDetailRequest request, Customer customer) {
		return ScalpingRequestDetail.builder()
			.customer(customer)
			.feedbackRequestedAt(LocalDate.now())
			.status(Status.NOT_YET)
			.isCourseCompleted(request.getIsCourseCompleted())
			.feedbackYear(request.getFeedbackYear())
			.feedbackMonth(request.getFeedbackMonth())
			.feedbackWeek(request.getFeedbackWeek())
			.isBestFeedback(false)
			.requestDate(request.getRequestDate())
			.category(request.getCategory())
			.dailyTradingCount(request.getDailyTradingCount())
			.riskTaking(request.getRiskTaking())
			.leverage(request.getLeverage())
			.totalPositionTakingCount(request.getTotalPositionTakingCount())
			.totalProfitMarginPerTrades(request.getTotalProfitMarginPerTrades())
			.positionStartReason(request.getPositionStartReason())
			.positionEndReason(request.getPositionEndReason())
			.trainerFeedbackRequestContent(request.getTrainerFeedbackRequestContent())
			.trendAnalysis(request.getTrendAnalysis())
			.build();
	}

	// ⭐ getFeedbackType() 구현
	@Override
	public FeedbackType getFeedbackType() {
		return FeedbackType.SCALPING;
	}
}
