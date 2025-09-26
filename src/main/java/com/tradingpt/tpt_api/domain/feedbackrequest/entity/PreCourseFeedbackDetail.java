package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 무료 고객 및 완강 전 고객이 공통으로 작성하는 피드백 상세 정보.
 */
@Getter
@Embeddable
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class PreCourseFeedbackDetail {

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

	public static PreCourseFeedbackDetail of(Double rnr, Integer operatingFundsRatio, BigDecimal entryPrice,
		BigDecimal exitPrice, BigDecimal settingStopLoss, BigDecimal settingTakeProfit, String positionStartReason,
		String positionEndReason) {
		return PreCourseFeedbackDetail.builder()
			.rnr(rnr)
			.operatingFundsRatio(operatingFundsRatio)
			.entryPrice(entryPrice)
			.exitPrice(exitPrice)
			.settingStopLoss(settingStopLoss)
			.settingTakeProfit(settingTakeProfit)
			.positionStartReason(positionStartReason)
			.positionEndReason(positionEndReason)
			.build();
	}

}
