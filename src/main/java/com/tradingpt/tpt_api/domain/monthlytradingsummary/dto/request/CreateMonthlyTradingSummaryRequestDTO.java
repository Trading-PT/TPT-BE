package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CreateMonthlyTradingSummaryRequestDTO {

	@Schema(description = "한 달 간 매매 최종 평가")
	private String monthlyEvaluation;

	@Schema(description = "다음 달 목표 성과")
	private String nextMonthGoal;
}
