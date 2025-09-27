package com.tradingpt.tpt_api.domain.monthlytradingsummary.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/trainers/me")
@RequiredArgsConstructor
@Tag(name = "트레이너가 특정 고객의 월간 매매 일지 생성")
public class MonthlyTradingSummaryV1Controller {

	@Operation(description = "특정 고객의 월별 매매 일지 생성")
	@PostMapping("/customers/{customerId}/monthly-trading-summaries/years/{year}/months/{month}")
	public BaseResponse<Void> createMonthlyTradingSummary(
	) {
		return BaseResponse.onSuccessCreate(null);
	}

}
