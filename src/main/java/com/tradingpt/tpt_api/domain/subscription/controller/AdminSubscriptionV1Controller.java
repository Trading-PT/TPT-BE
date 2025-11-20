package com.tradingpt.tpt_api.domain.subscription.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerSliceResponseDTO;
import com.tradingpt.tpt_api.domain.subscription.service.query.SubscriptionQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 관리자/트레이너 전용 구독 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@Tag(name = "어드민-구독관리", description = "관리자/트레이너 전용 구독 관리 API")
public class AdminSubscriptionV1Controller {

	private final SubscriptionQueryService subscriptionQueryService;

	@Operation(
		summary = "구독 고객 목록 조회",
		description = """
			활성 구독 고객 목록을 조회합니다.
			
			**필터 옵션:**
			- `myCustomersOnly=true`: 내가 담당하는 고객만 조회 (assignedTrainer = 본인)
			- `myCustomersOnly=false` 또는 생략: 모든 활성 구독 고객 조회
			
			**정렬 기준:**
			1. 멤버십 레벨 (PREMIUM 우선)
			2. 구독 생성일 (최신 순)
			
			**페이징:**
			- 무한 스크롤 방식 (Slice 사용)
			- 기본 20개, 최대 100개
			
			**권한:**
			- ADMIN: 모든 고객 조회 가능
			- TRAINER: 본인 담당 고객만 조회 가능 (myCustomersOnly=true 강제)
			"""
	)
	@GetMapping("/customers")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<SubscriptionCustomerSliceResponseDTO> getSubscriptionCustomers(
		@AuthenticationPrincipal(expression = "id")
		@Parameter(description = "트레이너 ID (자동 주입)", hidden = true)
		Long trainerId,

		@RequestParam(required = false, defaultValue = "false")
		@Parameter(description = "내 담당 고객만 조회 여부 (true: 본인 담당만, false: 전체)")
		Boolean myCustomersOnly,

		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
		@Parameter(description = "페이징 정보 (page, size)")
		Pageable pageable
	) {
		// 페이지 크기 제한 (최대 100)
		if (pageable.getPageSize() > 100) {
			pageable = Pageable.ofSize(100).withPage(pageable.getPageNumber());
		}

		Slice<SubscriptionCustomerResponseDTO> slice = subscriptionQueryService
			.getActiveSubscriptionCustomers(trainerId, myCustomersOnly, pageable);

		return BaseResponse.onSuccess(SubscriptionCustomerSliceResponseDTO.from(slice));
	}
}
