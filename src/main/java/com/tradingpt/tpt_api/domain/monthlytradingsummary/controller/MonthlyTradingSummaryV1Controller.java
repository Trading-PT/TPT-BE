package com.tradingpt.tpt_api.domain.monthlytradingsummary.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.YearlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.service.query.MonthlyTradingSummaryQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/monthly-trading-summaries")
@RequiredArgsConstructor
@Tag(name = "월간 매매 일지 통계", description = "피드백 요청 월간 매매 일지 통계 API")
public class MonthlyTradingSummaryV1Controller {

	private final MonthlyTradingSummaryQueryService monthlyTradingSummaryQueryService;

	@Operation(summary = "해당 연도에 대한 월 리스트",
		description = "해당 연도에 대한 피드백 요청이 존재하는 월을 리스트업 합니다.")
	@GetMapping("/customers/me/years/{year}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<YearlySummaryResponseDTO> getYearlySummaryResponse(
		@PathVariable Integer year,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(
			monthlyTradingSummaryQueryService.getYearlySummaryResponse(year, customerId));
	}

	@Operation(summary = "해당 연/월에 대한 매매 일지 통계 조회",
		description = "해당 연/월에 대한 CourseStatus별 매매 일지 통계를 조회합니다. 한 달 중간에 완강 상태가 변경된 경우 여러 CourseStatus별 통계가 반환됩니다.")
	@GetMapping("/customers/me/years/{year}/months/{month}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<MonthlySummaryResponseDTO> getMonthlySummaryResponse(
		@PathVariable Integer year,
		@PathVariable Integer month,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(
			monthlyTradingSummaryQueryService.getMonthlySummaryResponse(year, month, customerId)
		);
	}
}
