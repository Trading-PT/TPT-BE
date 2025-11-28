package com.tradingpt.tpt_api.domain.weeklytradingsummary.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request.CreateWeeklyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyLossFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklyProfitFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.service.command.WeeklyTradingSummaryCommandService;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query.WeeklyTradingSummaryQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/weekly-trading-summary")
@RequiredArgsConstructor
@Tag(name = "주간 매매 일지 통계", description = "피드백 요청 주간 매매 일지 통계 API")
public class WeeklyTradingSummaryV1Controller {

	private final WeeklyTradingSummaryQueryService weeklyTradingSummaryQueryService;
	private final WeeklyTradingSummaryCommandService weeklyTradingSummaryCommandService;

	@Operation(description = "주간 매매 일지")
	@GetMapping("/customers/me/years/{year}/months/{month}/weeks/{week}")
	public BaseResponse<WeeklySummaryResponseDTO> getWeeklyTradingSummary(
		@PathVariable Integer year,
		@PathVariable Integer month,
		@PathVariable Integer week,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(weeklyTradingSummaryQueryService.getWeeklyTradingSummary(
			year, month, week, customerId)
		);
	}

	@Operation(
		summary = "주간 매매 일지 통계 작성 (Customer)",
		description = """
			고객이 자신의 주간 매매 일지 통계를 작성합니다.
			
			⭐ 작성 규칙:
			
			1. 완강 전 (BEFORE_COMPLETION):
			   ✅ memo: 필수
			   ❌ 상세 평가: 불가
			
			2. 완강 후 (AFTER_COMPLETION):
			   🚫 고객은 작성 불가 (트레이너만 작성)
			
			제약 조건:
			- 해당 주의 코스 상태와 투자 타입은 첫 번째 피드백 기준
			- 이미 해당 주에 통계가 존재하면 생성 불가
			"""
	)
	@PostMapping("/customers/me/years/{year}/months/{month}/weeks/{week}")
	public BaseResponse<Void> createWeeklySummaryByCustomer(
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "8", required = true)
		@PathVariable Integer month,
		@Parameter(description = "주 (1-5)", example = "3", required = true)
		@PathVariable Integer week,
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@Valid @RequestBody CreateWeeklyTradingSummaryRequestDTO request
	) {
		return BaseResponse.onSuccessCreate(
			weeklyTradingSummaryCommandService.createWeeklyTradingSummaryByCustomer(
				year, month, week, customerId, request)
		);
	}

	@Operation(
		summary = "주간 이익 매매 모아보기 (Customer)",
		description = """
			특정 주의 이익 매매 피드백 목록을 조회합니다.
			
			📊 조회 조건:
			- 완강 전 (BEFORE_COMPLETION) 피드백만 조회
			- P&L > 0인 이익 매매만 필터링
			- feedbackRequestDate 기준 내림차순 정렬
			
			반환 정보:
			- 피드백 ID, 제목, 요청 날짜, P&L
			- 투자 타입, 상태, 응답 여부
			"""
	)
	@GetMapping("/customers/me/years/{year}/months/{month}/weeks/{week}/profit-feedbacks")
	public BaseResponse<WeeklyProfitFeedbackListResponseDTO> getProfitFeedbacks(
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "11", required = true)
		@PathVariable Integer month,
		@Parameter(description = "주 (1-5)", example = "3", required = true)
		@PathVariable Integer week,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(
			weeklyTradingSummaryQueryService.getProfitFeedbacksByWeek(
				year, month, week, customerId)
		);
	}

	@Operation(
		summary = "주간 손실 매매 모아보기 (Customer)",
		description = """
			특정 주의 손실 매매 피드백 목록을 조회합니다.
			
			📊 조회 조건:
			- 완강 전 (BEFORE_COMPLETION) 피드백만 조회
			- P&L < 0인 손실 매매만 필터링
			- feedbackRequestDate 기준 내림차순 정렬
			
			반환 정보:
			- 피드백 ID, 제목, 요청 날짜, P&L
			- 투자 타입, 상태, 응답 여부
			"""
	)
	@GetMapping("/customers/me/years/{year}/months/{month}/weeks/{week}/loss-feedbacks")
	public BaseResponse<WeeklyLossFeedbackListResponseDTO> getLossFeedbacks(
		@Parameter(description = "연도", example = "2025", required = true)
		@PathVariable Integer year,
		@Parameter(description = "월 (1-12)", example = "11", required = true)
		@PathVariable Integer month,
		@Parameter(description = "주 (1-5)", example = "3", required = true)
		@PathVariable Integer week,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(
			weeklyTradingSummaryQueryService.getLossFeedbacksByWeek(
				year, month, week, customerId)
		);
	}
}
