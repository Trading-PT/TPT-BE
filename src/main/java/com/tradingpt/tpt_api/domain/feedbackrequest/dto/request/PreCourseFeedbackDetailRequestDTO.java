package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.PreCourseFeedbackDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "완강 전 고객 피드백 상세 입력")
public class PreCourseFeedbackDetailRequestDTO {

	@Schema(description = "R&R")
	private Double rnr;

	@Schema(description = "운용 자금 대비 비중 (%)")
	private Integer operatingFundsRatio;

	@Schema(description = "진입 금액")
	private BigDecimal entryPrice;

	@Schema(description = "탈출 금액")
	private BigDecimal exitPrice;

	@Schema(description = "설정 손절가")
	private BigDecimal settingStopLoss;

	@Schema(description = "설정 익절가")
	private BigDecimal settingTakeProfit;

	@Schema(description = "포지션 진입 근거")
	private String positionStartReason;

	@Schema(description = "포지션 탈출 근거")
	private String positionEndReason;

	@JsonIgnore
	public boolean isEmpty() {
		return rnr == null
			&& operatingFundsRatio == null
			&& entryPrice == null
			&& exitPrice == null
			&& settingStopLoss == null
			&& settingTakeProfit == null
			&& (positionStartReason == null || positionStartReason.isBlank())
			&& (positionEndReason == null || positionEndReason.isBlank());
	}

	@JsonIgnore
	public PreCourseFeedbackDetail toEntity() {
		if (isEmpty()) {
			return null;
		}

		return PreCourseFeedbackDetail.of(rnr, operatingFundsRatio, entryPrice, exitPrice, settingStopLoss,
			settingTakeProfit, positionStartReason, positionEndReason);
	}

}
