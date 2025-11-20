package com.tradingpt.tpt_api.domain.investmenttypehistory.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.InvestmentTypeHistoryResponseDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.service.query.InvestmentTypeHistoryQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 투자유형 이력 관리 API (관리자용)
 * RESTful 리소스: InvestmentTypeHistory (Admin)
 */
@RestController
@RequestMapping("/api/v1/admin/investment-type-histories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
@Tag(name = "어드민-투자유형 이력 관리", description = "투자유형 변경 이력 관리 API (관리자)")
public class AdminInvestmentTypeHistoryV1Controller {

	private final InvestmentTypeHistoryQueryService investmentTypeHistoryQueryService;

	@Operation(
		summary = "특정 고객의 투자유형 이력 조회 (관리자)",
		description = """
			특정 고객의 투자유형 변경 이력을 조회합니다.

			**특징:**
			- 시작일(startDate) 기준 오름차순으로 정렬됩니다.
			- endDate가 null이면 현재 진행 중인 투자유형입니다.
			- 투자유형: SWING(스윙), DAY(데이), SCALPING(스켈핑)

			**권한:**
			- 관리자 및 트레이너만 접근 가능합니다.
			"""
	)
	@GetMapping("/{customerId}")
	public BaseResponse<List<InvestmentTypeHistoryResponseDTO>> getCustomerInvestmentTypeHistories(
		@Parameter(description = "고객 ID", required = true)
		@PathVariable Long customerId
	) {
		return BaseResponse.onSuccess(
			investmentTypeHistoryQueryService.getCustomerInvestmentTypeHistories(customerId));
	}
}
