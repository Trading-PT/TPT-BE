package com.tradingpt.tpt_api.domain.subscriptionplan.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.subscriptionplan.dto.request.SubscriptionPlanCreateRequestDTO;
import com.tradingpt.tpt_api.domain.subscriptionplan.service.command.SubscriptionPlanCommandService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 구독 플랜 관리 REST Controller (어드민 전용)
 */
@RestController
@RequestMapping("/api/v1/admin/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "결제 플랜 관리", description = "어드민 전용 결제 플랜 관리 API")
public class AdminSubscriptionPlanV1Controller {

	private final SubscriptionPlanCommandService subscriptionPlanCommandService;

	@Operation(
		summary = "구독 플랜 등록",
		description = """
			새로운 구독 플랜을 등록합니다.
			- 기존 활성 플랜이 있다면 자동으로 비활성화됩니다.
			- 새로 등록하는 플랜이 활성 플랜이 됩니다.
			"""
	)
	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public BaseResponse<Long> createSubscriptionPlan(
		@Valid @RequestBody SubscriptionPlanCreateRequestDTO request
	) {
		return BaseResponse.onSuccessCreate(subscriptionPlanCommandService.createSubscriptionPlan(request));
	}

}
