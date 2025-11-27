package com.tradingpt.tpt_api.domain.investmenttypehistory.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.InvestmentTypeHistoryResponseDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.service.query.InvestmentTypeHistoryQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 투자유형 이력 조회 API (고객용)
 * RESTful 리소스: InvestmentTypeHistory
 */
@RestController
@RequestMapping("/api/v1/investment-type-histories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
@Tag(name = "투자유형 이력", description = "투자유형 변경 이력 조회 API")
public class InvestmentTypeHistoryV1Controller {

	private final InvestmentTypeHistoryQueryService investmentTypeHistoryQueryService;

	@Operation(
		summary = "내 투자유형 이력 조회",
		description = """
			본인의 투자유형 변경 이력을 조회합니다.

			**특징:**
			- 시작일(startDate) 기준 오름차순으로 정렬됩니다.
			- endDate가 null이면 현재 진행 중인 투자유형입니다.
			- 투자유형: SWING(스윙), DAY(데이)
			"""
	)
	@GetMapping
	public BaseResponse<List<InvestmentTypeHistoryResponseDTO>> getMyInvestmentTypeHistories(
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(
			investmentTypeHistoryQueryService.getCustomerInvestmentTypeHistories(customerId));
	}
}
