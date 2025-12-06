package com.tradingpt.tpt_api.domain.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationListResponseDTO;
import com.tradingpt.tpt_api.domain.user.service.query.CustomerEvaluationQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/admin/customer-evaluations")
@RequiredArgsConstructor
@Tag(name = "관리자 - 고객 평가 관리", description = "ADMIN/TRAINER 전용 고객 평가 관리 API")
public class AdminCustomerEvaluationV1Controller {

	private final CustomerEvaluationQueryService customerEvaluationQueryService;

	/**
	 * 미작성 평가 목록 조회 (무한 스크롤)
	 *
	 * <p>역할별 동작:
	 * <ul>
	 *   <li>ADMIN: 모든 PREMIUM 고객의 미작성 평가 목록 조회</li>
	 *   <li>TRAINER: 자기 담당 PREMIUM 고객의 미작성 평가 목록만 조회</li>
	 * </ul>
	 *
	 * <p>비즈니스 규칙:
	 * <ul>
	 *   <li>PREMIUM 멤버십 고객만 조회</li>
	 *   <li>FeedbackRequest가 존재하는 월의 미작성 평가만 표시</li>
	 *   <li>월간 평가: DAY/SWING 모두 대상</li>
	 *   <li>주간 평가: DAY만 대상, 현재 월은 현재 주차까지만 표시</li>
	 *   <li>고객 이름순 정렬</li>
	 *   <li>무한 스크롤 (Slice 페이징)</li>
	 * </ul>
	 *
	 * @param userId   현재 로그인한 사용자 ID (ADMIN 또는 TRAINER, 자동 주입)
	 * @param pageable 페이징 정보 (기본: 20개씩)
	 * @return 미작성 평가 목록 (고객별 평가 대상을 행으로 나열)
	 */
	@Operation(
		summary = "미작성 평가 목록 조회",
		description = """
			미작성 평가 목록을 조회합니다.

			**역할별 동작:**
			- ADMIN: 모든 PREMIUM 고객의 미작성 평가 목록 조회
			- TRAINER: 자기 담당 PREMIUM 고객의 미작성 평가 목록만 조회

			**조회 범위:**
			- PREMIUM 멤버십 고객만 대상
			- FeedbackRequest가 존재하는 월의 미작성 평가만 표시

			**평가 유형:**
			- 월간 평가: DAY/SWING 투자 유형 모두 대상
			- 주간 평가: DAY 투자 유형만 대상 (현재 월은 현재 주차까지만)

			**정렬 및 페이징:**
			- 고객 이름순(오름차순) 정렬
			- 무한 스크롤 방식 (Slice 페이징)
			- 기본 20개씩 조회

			**응답 형식:**
			- 각 행은 하나의 평가 대상 (고객 1명당 N개 평가 = N개 행)
			- 평가 타입: WEEKLY(주간), MONTHLY(월간)
			- 평가 기간 표시: "2025년 11월 월간 평가", "2025년 11월 3주차 주간 평가"
			"""
	)
	@GetMapping("/pending")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<PendingEvaluationListResponseDTO> getPendingEvaluations(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long userId,
		@PageableDefault(size = 20) Pageable pageable
	) {
		PendingEvaluationListResponseDTO result =
			customerEvaluationQueryService.getPendingEvaluations(userId, pageable);
		return BaseResponse.onSuccess(result);
	}
}
