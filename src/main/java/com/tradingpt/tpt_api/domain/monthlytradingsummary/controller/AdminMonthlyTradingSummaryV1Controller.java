package com.tradingpt.tpt_api.domain.monthlytradingsummary.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.CreateMonthlyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.UpsertMonthlyEvaluationRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyEvaluationResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyWeekFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.service.command.MonthlyTradingSummaryCommandService;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.service.query.MonthlyTradingSummaryQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/monthly-trading-summaries")
@RequiredArgsConstructor
@Tag(name = "ADMIN 월간 매매 일지 통계", description = "ADMIN이 사용하는 월간 매매 일지 통계 API")
public class AdminMonthlyTradingSummaryV1Controller {

	private final MonthlyTradingSummaryQueryService monthlyTradingSummaryQueryService;
	private final MonthlyTradingSummaryCommandService monthlyTradingSummaryCommandService;

	@Deprecated
	@Operation(
		summary = "[Deprecated] 해당 연/월에 대한 데이, 스윙 트레이딩 타입 피드백 통계",
		description = """
			⚠️ **Deprecated**: PUT /customers/{customerId}/years/{year}/months/{month}/evaluation API를 사용하세요.

			해당 연/월에 대한 데이, 스윙 트레이딩 타입 피드백 통계에 피드백을 남김
			""",
		deprecated = true
	)
	@PostMapping("/customers/{customerId}/years/{year}/months/{month}")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<Void> createMonthlySummary(
		@PathVariable Long customerId,
		@PathVariable Integer year,
		@PathVariable Integer month,
		@Valid @RequestBody CreateMonthlyTradingSummaryRequestDTO request
	) {
		return BaseResponse.onSuccessCreate(
			monthlyTradingSummaryCommandService.createMonthlySummary(year, month, customerId, request)
		);
	}

	@Operation(summary = "해당 연도에 대한 월 리스트",
		description = "해당 연도에 대한 피드백 요청이 존재하는 월을 리스트업 합니다.")
	@GetMapping("/customers/{customerId}/years/{year}")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<YearlySummaryResponseDTO> getYearlySummaryResponse(
		@PathVariable Integer year,
		@PathVariable Long customerId,
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(

			// TODO: 내 담당 고객의 피드백만 볼 수 있도록 검증 로직을 추가해야 한다.
			monthlyTradingSummaryQueryService.getYearlySummaryResponse(year, customerId));
	}

	@Operation(
		summary = "해당 연/월에 대한 주차 리스트업",
		description = "해당 연/월에 대한 주차를 리스트업 합니다."
	)
	@GetMapping("/customers/{customerId}/years/{year}/months/{month}")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<MonthlyWeekFeedbackResponseDTO> getMonthlyWeekFeedbackResponse(
		@PathVariable Integer year,
		@PathVariable Integer month,
		@PathVariable Long customerId,
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(
			monthlyTradingSummaryQueryService.getMonthlyWeekFeedbackResponse(
				year, month, customerId, trainerId
			)
		);
	}

	@Operation(
		summary = "월간 매매일지 트레이너 평가 Upsert (Trainer)",
		description = """
			트레이너가 고객의 월간 매매일지 평가를 생성하거나 수정합니다. (Upsert 패턴)

			⭐ 작성 규칙:

			✅ 완강 후 (AFTER_COMPLETION):
			   - DAY 타입: 트레이너 평가 작성/수정 가능
			   - SWING 타입: 트레이너 평가 작성/수정 가능
			   - monthlyEvaluation: 한 달 간 매매 최종 평가
			   - nextMonthGoal: 다음 달 목표 성과

			❌ 완강 전 (BEFORE_COMPLETION, PENDING_COMPLETION):
			   - 트레이너는 평가 불가

			동작 방식:
			- 해당 월에 통계가 없으면 새로 생성
			- 해당 월에 통계가 있으면 평가만 수정 (JPA Dirty Checking)
			"""
	)
	@PutMapping("/customers/{customerId}/years/{year}/months/{month}/evaluation")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TRAINER')")
	public BaseResponse<MonthlyEvaluationResponseDTO> upsertMonthlyEvaluationByTrainer(
		@PathVariable Long customerId,
		@PathVariable Integer year,
		@PathVariable Integer month,
		@AuthenticationPrincipal(expression = "id") Long trainerId,
		@Valid @RequestBody UpsertMonthlyEvaluationRequestDTO request
	) {
		return BaseResponse.onSuccess(
			monthlyTradingSummaryCommandService.upsertMonthlyEvaluationByTrainer(
				year, month, customerId, trainerId, request)
		);
	}
}
