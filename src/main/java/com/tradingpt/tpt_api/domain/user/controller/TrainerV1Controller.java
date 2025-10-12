package com.tradingpt.tpt_api.domain.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.user.dto.response.CustomerEvaluationResponseDTO;
import com.tradingpt.tpt_api.domain.user.service.query.TrainerQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "트레이너", description = "트레이너가 사용하는 API")
public class TrainerV1Controller {

	private final TrainerQueryService trainerQueryService;

	@Operation(summary = "트레이너가 담당하는 고객을 조회한다.", description = "트레이너가 담당하는 고객 조회 API")
	@GetMapping("/me/managed_customers/evaluations")
	@PreAuthorize("hasRole('ROLE_TRAINER') or hasRole('ROLE_ADMIN')")
	public BaseResponse<Page<CustomerEvaluationResponseDTO>> getManagedCustomersEvaluations(
		@PageableDefault(size = 20, sort = "name") Pageable pageable,
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(trainerQueryService.getManagedCustomersEvaluations(pageable, trainerId));
	}
}
