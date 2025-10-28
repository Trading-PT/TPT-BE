package com.tradingpt.tpt_api.domain.monthlytradingsummary.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.CreateMonthlyTradingSummaryRequestDTO;
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

	@Operation(summary = "해당 연/월에 대한 데이, 스윙 트레이딩 타입 피드백 통계",
		description = "해당 연/월에 대한 데이, 스윙 트레이딩 타입 피드백 통계에 피드백을 남김")
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

}
