package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "주간 매매일지 트레이너 평가 Upsert 요청 DTO (트레이너용 - 완강 후 + DAY 타입)")
public class UpsertWeeklyEvaluationRequestDTO {

	@Size(max = 5000, message = "주간 매매 평가는 5000자를 초과할 수 없습니다.")
	@Schema(description = "주간 회원 매매 평가",
		example = "이번 주 매매는 전반적으로 손익비가 좋았으나, 진입 타이밍을 조금 더 기다렸으면 좋았을 것입니다.")
	private String weeklyEvaluation;

	@Size(max = 5000, message = "수익 매매 분석은 5000자를 초과할 수 없습니다.")
	@Schema(description = "수익난 매매 분석",
		example = "월요일 AAPL 매매에서 추세 전환을 잘 포착하여 좋은 수익을 냈습니다.")
	private String weeklyProfitableTradingAnalysis;

	@Size(max = 5000, message = "손실 매매 분석은 5000자를 초과할 수 없습니다.")
	@Schema(description = "손실난 매매 분석",
		example = "수요일 TSLA 매매에서 역추세 진입으로 손실이 발생했습니다. 추세 방향 확인 후 진입이 필요합니다.")
	private String weeklyLossTradingAnalysis;
}
