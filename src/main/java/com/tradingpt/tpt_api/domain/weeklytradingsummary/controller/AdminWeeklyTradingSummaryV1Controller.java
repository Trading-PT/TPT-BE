package com.tradingpt.tpt_api.domain.weeklytradingsummary.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.CreateWeeklyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.service.command.WeeklyTradingSummaryCommandService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/weekly-trading-summary")
@RequiredArgsConstructor
@Tag(name = "ADMIN 주간 매매 일지 통계", description = "ADMIN이 사용하는 주간 매매 일지 통계 API")
public class AdminWeeklyTradingSummaryV1Controller {

	private final WeeklyTradingSummaryCommandService weeklyTradingSummaryCommandService;

	@Operation(
		summary = "주간 매매 일지 통계 작성 (Trainer)",
		description = """
			트레이너가 주간 매매 일지 통계를 작성합니다.
			
			⭐ 작성 규칙:
			
			1. 완강 전 (BEFORE_COMPLETION):
			   🚫 트레이너는 작성 불가 (고객이 memo 작성)
			
			2. 완강 후 (AFTER_COMPLETION) + DAY 유형:
			   ❌ memo: 불가
			   ✅ weeklyEvaluation: 필수
			   ✅ weeklyProfitableTradingAnalysis: 필수
			   ✅ weeklyLossTradingAnalysis: 필수
			
			3. 완강 후 (AFTER_COMPLETION) + SCALPING/SWING 유형:
			   🚫 작성 불가
			
			제약 조건:
			- 해당 주의 코스 상태와 투자 타입은 첫 번째 피드백 기준
			- 이미 해당 주에 통계가 존재하면 생성 불가
			"""
	)
	@PostMapping("/customers/{customerId}/years/{year}/months/{month}/weeks/{week}")
	public BaseResponse<Void> createWeeklySummaryByTrainer(
		@Parameter(description = "고객 ID", required = true)
		@PathVariable Long customerId,
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "8", required = true)
		@PathVariable Integer month,
		@Parameter(description = "주 (1-5)", example = "3", required = true)
		@PathVariable Integer week,
		@AuthenticationPrincipal(expression = "id") Long trainerId,
		@Valid @RequestBody CreateWeeklyTradingSummaryRequestDTO request
	) {
		return BaseResponse.onSuccessCreate(
			weeklyTradingSummaryCommandService.createWeeklyTradingSummaryByTrainer(
				year, month, week, customerId, trainerId, request)
		);
	}
}
