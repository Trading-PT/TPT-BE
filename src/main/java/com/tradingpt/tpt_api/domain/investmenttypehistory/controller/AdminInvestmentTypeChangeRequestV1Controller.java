package com.tradingpt.tpt_api.domain.investmenttypehistory.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request.ApproveChangeRequestDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.ChangeRequestResponseDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.service.command.InvestmentTypeChangeCommandService;
import com.tradingpt.tpt_api.domain.investmenttypehistory.service.query.InvestmentTypeChangeQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 투자 유형 변경 신청 관리 API (어드민용)
 * RESTful 리소스: InvestmentTypeChangeRequest (Admin Management)
 */
@RestController
@RequestMapping("/api/v1/admin/investment-type-change-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
@Tag(name = "어드민 투자 유형 변경 관리", description = "어드민용 투자 유형 변경 신청 관리 API")
public class AdminInvestmentTypeChangeRequestV1Controller {

	private final InvestmentTypeChangeCommandService investmentTypeChangeCommandService;
	private final InvestmentTypeChangeQueryService investmentTypeChangeQueryService;

	@Operation(
		summary = "대기 중인 변경 신청 목록 조회",
		description = "승인 대기 중인 모든 투자 유형 변경 신청을 조회합니다."
	)
	@GetMapping("/pending")
	public BaseResponse<List<ChangeRequestResponseDTO>> getPendingChangeRequests() {
		return BaseResponse.onSuccess(
			investmentTypeChangeQueryService.getPendingChangeRequests());
	}

	@Operation(
		summary = "모든 변경 신청 목록 조회",
		description = "모든 상태의 투자 유형 변경 신청을 조회합니다."
	)
	@GetMapping
	public BaseResponse<List<ChangeRequestResponseDTO>> getAllChangeRequests() {
		return BaseResponse.onSuccess(
			investmentTypeChangeQueryService.getAllChangeRequests());
	}

	@Operation(
		summary = "변경 신청 상세 조회",
		description = "특정 변경 신청의 상세 정보를 조회합니다."
	)
	@GetMapping("/{requestId}")
	public BaseResponse<ChangeRequestResponseDTO> getChangeRequestDetail(
		@PathVariable Long requestId
	) {
		return BaseResponse.onSuccess(
			investmentTypeChangeQueryService.getChangeRequestDetail(requestId));
	}

	@Operation(
		summary = "변경 신청 승인/거부",
		description = """
			투자 유형 변경 신청을 승인하거나 거부합니다.
			
			승인 시:
			- 기존 InvestmentTypeHistory가 변경 예정일 전날로 종료됩니다.
			- 새로운 InvestmentTypeHistory가 변경 예정일부터 시작됩니다.
			- Customer의 primaryInvestmentType이 변경됩니다.
			
			거부 시:
			- 거부 사유를 반드시 입력해야 합니다.
			- 고객에게 거부 사유가 표시됩니다.
			"""
	)
	@PatchMapping("/{requestId}/process")
	public BaseResponse<ChangeRequestResponseDTO> processChangeRequest(
		@PathVariable Long requestId,
		@AuthenticationPrincipal(expression = "id") Long trainerId,
		@Valid @RequestBody ApproveChangeRequestDTO request
	) {
		return BaseResponse.onSuccess(
			investmentTypeChangeCommandService.processChangeRequest(requestId, trainerId, request));
	}
}