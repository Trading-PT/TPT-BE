package com.tradingpt.tpt_api.domain.investmenttypehistory.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request.CreateChangeRequestDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.ChangeRequestResponseDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.service.command.InvestmentTypeChangeCommandService;
import com.tradingpt.tpt_api.domain.investmenttypehistory.service.query.InvestmentTypeChangeQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 투자 유형 변경 신청 API (고객용)
 * RESTful 리소스: InvestmentTypeChangeRequest
 */
@RestController
@RequestMapping("/api/v1/investment-type-change-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
@Tag(name = "투자 유형 변경 신청", description = "투자 유형 변경 신청 관련 API")
public class InvestmentTypeChangeRequestV1Controller {

	private final InvestmentTypeChangeCommandService investmentTypeChangeCommandService;
	private final InvestmentTypeChangeQueryService investmentTypeChangeQueryService;

	@Operation(
		summary = "투자 유형 변경 신청",
		description = """
			투자 유형 변경을 신청합니다.
			- 변경은 다음 달 1일부터 적용됩니다.
			- 어드민의 승인이 필요합니다.
			- 대기 중인 신청이 있으면 추가 신청이 불가능합니다.
			"""
	)
	@PostMapping
	public BaseResponse<ChangeRequestResponseDTO> createChangeInvestmentTypeRequest(
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@Valid @RequestBody CreateChangeRequestDTO request
	) {
		return BaseResponse.onSuccessCreate(
			investmentTypeChangeCommandService.createChangeRequest(customerId, request));
	}

	@Operation(
		summary = "내 변경 신청 목록 조회",
		description = "내가 신청한 모든 투자 유형 변경 신청 내역을 조회합니다."
	)
	@GetMapping
	public BaseResponse<List<ChangeRequestResponseDTO>> getMyChangeRequests(
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(
			investmentTypeChangeQueryService.getMyChangeRequests(customerId));
	}

	@Operation(
		summary = "변경 신청 상세 조회",
		description = "특정 변경 신청의 상세 정보를 조회합니다."
	)
	@GetMapping("/{requestId}")
	public BaseResponse<ChangeRequestResponseDTO> getChangeRequest(
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@PathVariable Long requestId
	) {
		return BaseResponse.onSuccess(
			investmentTypeChangeQueryService.getChangeRequest(customerId, requestId));
	}

	@Operation(
		summary = "변경 신청 취소",
		description = "대기 중인 변경 신청을 취소합니다. 이미 처리된 신청은 취소할 수 없습니다."
	)
	@DeleteMapping("/{requestId}")
	public BaseResponse<Void> cancelChangeRequest(
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@PathVariable Long requestId
	) {
		investmentTypeChangeCommandService.cancelChangeRequest(customerId, requestId);
		return BaseResponse.onSuccess(null);
	}
}