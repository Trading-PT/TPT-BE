package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "주간 매매 일지 통계 작성 요청 DTO")
public class CreateWeeklyTradingSummaryRequestDTO {

	@Schema(description = "나의 문제점 메모 (완강 전 - 고객만)",
		example = "이번 주는 손절을 너무 늦게 했습니다.")
	private String memo;

	@Schema(description = "주간 회원 매매 평가 (완강 후 + DAY - 트레이너만)",
		example = "이번 주 매매는 전반적으로 좋았으나...")
	private String weeklyEvaluation;

	@Schema(description = "수익난 매매 분석 (완강 후 + DAY - 트레이너만)",
		example = "월요일 AAPL 매매에서...")
	private String weeklyProfitableTradingAnalysis;

	@Schema(description = "손실난 매매 분석 (완강 후 + DAY - 트레이너만)",
		example = "수요일 TSLA 매매에서...")
	private String weeklyLossTradingAnalysis;
}