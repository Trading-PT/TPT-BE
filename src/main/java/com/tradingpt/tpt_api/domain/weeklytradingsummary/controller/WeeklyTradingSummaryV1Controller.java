package com.tradingpt.tpt_api.domain.weeklytradingsummary.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response.WeeklySummaryResponseDTO;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.service.query.WeeklyTradingSummaryQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/weekly-trading-summary")
@RequiredArgsConstructor
@Tag(name = "주간 매매 일지 통계", description = "피드백 요청 주간 매매 일지 통계 API")
public class WeeklyTradingSummaryV1Controller {

	private final WeeklyTradingSummaryQueryService weeklyTradingSummaryQueryService;

	@Operation(description = "주간 매매 일지")
	@GetMapping("/customers/me/years/{year}/months/{month}/weeks/{week}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
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
}
