package com.tradingpt.tpt_api.domain.user.controller;

import com.tradingpt.tpt_api.domain.user.dto.request.UidUpdateRequestDTO;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.user.dto.request.GiveUserTokenRequestDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.FreeCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.MyCustomerListResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.NewSubscriptionCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.PendingUserApprovalRowResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.UserStatusUpdateResponseDTO;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.service.command.AdminUserCommandService;
import com.tradingpt.tpt_api.domain.user.service.query.AdminUserQueryService;
import com.tradingpt.tpt_api.domain.user.service.query.CustomerQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "관리자 - 회원 관리", description = "관리자/트레이너 전용 회원 관리 API")
public class AdminUserV1Controller {

	private final AdminUserCommandService adminUserCommandService;
	private final AdminUserQueryService adminUserQueryService;
	private final CustomerQueryService customerQueryService;

	@Operation(summary = "신규 가입자 목록(UID 검토 중) 조회")
	@GetMapping("/pending")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public ResponseEntity<BaseResponse<List<PendingUserApprovalRowResponseDTO>>> getPendingUsers() {
		List<PendingUserApprovalRowResponseDTO> result = adminUserQueryService.getPendingApprovalRows();
		return ResponseEntity.ok(BaseResponse.onSuccess(result));
	}

	@Operation(summary = "신규 가입자 UID 승인 여부 처리 (승인/거절)")
	@PatchMapping("/{userId}/status")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public ResponseEntity<BaseResponse<UserStatusUpdateResponseDTO>> updateUserStatus(
		@PathVariable Long userId,
		@RequestParam UserStatus status
	) {
		adminUserCommandService.updateUserStatus(userId, status);
		UserStatusUpdateResponseDTO response = UserStatusUpdateResponseDTO.of(userId);
		return ResponseEntity.ok(BaseResponse.onSuccess(response));
	}

	@Operation(
			summary = "UID로 회원 검색 (페이지네이션)",
			description = """
        입력한 UID 문자열로 시작하는 회원 목록을 페이지네이션으로 조회합니다.

        예시:
        - GET /api/v1/admin/users/search-by-uid?uid=abc
        - GET /api/v1/admin/users/search-by-uid?uid=abc&page=0&size=20
        """
	)
	@GetMapping("/search-by-uid")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public ResponseEntity<BaseResponse<Page<PendingUserApprovalRowResponseDTO>>> searchUsersByUid(
			@RequestParam("uid") String uidPrefix,
			@PageableDefault(size = 20) Pageable pageable
	) {
		Page<PendingUserApprovalRowResponseDTO> result =
				adminUserQueryService.searchUsersByUidPrefix(uidPrefix, pageable);

		return ResponseEntity.ok(BaseResponse.onSuccess(result));
	}



	@Operation(summary = "특정 유저 UID 값 변경", description = "관리자/트레이너가 고객의 UID 문자열을 수정합니다.")
	@PatchMapping("/{userId}/uid")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public ResponseEntity<BaseResponse<Void>> updateUserUid(
			@PathVariable Long userId,
			@Valid @RequestBody UidUpdateRequestDTO request
	) {
		adminUserCommandService.updateUserUid(userId, request.getUid());
		return ResponseEntity.ok(BaseResponse.onSuccess(null));
	}

	@Operation(summary = "고객의 토큰 부여 기능", description = "고객에게 토큰을 일정 개수만큼 발급합니다.")
	@PatchMapping("/{userId}/token")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<Void> giveUserTokens(
		@PathVariable Long userId,
		@Valid @RequestBody GiveUserTokenRequestDTO request
	) {
		adminUserCommandService.giveUserTokens(userId, request);
		return BaseResponse.onSuccess(null);
	}

	@Operation(
		summary = "내 담당 고객 목록 조회 (무한 스크롤)",
		description = """
			트레이너가 담당하는 고객 목록을 무한 스크롤 방식으로 조회합니다.

			특징:
			- 자신이 담당하는 고객만 조회
			- 고객 기본 정보 (이름, 전화번호, 투자 유형, 멤버십, 토큰)
			- 최신 배정 순으로 정렬
			- Slice 기반 무한 스크롤 지원

			사용 시나리오:
			- 내 담당 고객 관리 페이지
			- 각 고객별로 피드백 내역, 과제 관리, 평가 관리 가능

			페이징 파라미터:
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지 크기 (기본값: 20)

			예시:
			- GET /api/v1/admin/customers/my-customers
			- GET /api/v1/admin/customers/my-customers?page=0&size=20
			"""
	)
	@GetMapping("/my-customers")
	public BaseResponse<MyCustomerListResponseDTO> getMyCustomers(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long trainerId,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return BaseResponse.onSuccess(
			customerQueryService.getMyCustomers(trainerId, pageable)
		);
	}

	@Operation(
		summary = "미구독(무료) 고객 목록 조회",
		description = """
			미구독 상태의 무료 고객 목록을 조회합니다.

			미구독 고객 정의:
			1. Subscription이 없거나 ACTIVE 상태가 아닌 고객
			2. membershipLevel이 BASIC인 고객
			3. 담당 트레이너가 없는 고객 (assignedTrainer IS NULL)

			조회 정보:
			- 고객 ID, 이름, 전화번호
			- 현재 투자 유형 (SCALPING, DAY, SWING)
			- 보유 토큰 수
			- 가입일시

			정렬 옵션 (sort 파라미터):
			- createdAt,desc: 최근 가입 순 (기본값)
			- createdAt,asc: 오래된 가입 순
			- name,asc: 이름 오름차순
			- name,desc: 이름 내림차순
			- tokenCount,desc: 토큰 많은 순
			- tokenCount,asc: 토큰 적은 순

			페이징:
			- Slice 방식 (무한 스크롤)
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지 크기 (기본값: 20)

			예시:
			- GET /api/v1/admin/users/free-customers
			- GET /api/v1/admin/users/free-customers?page=0&size=20&sort=createdAt,desc
			- GET /api/v1/admin/users/free-customers?sort=tokenCount,desc
			"""
	)
	@GetMapping("/free-customers")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<Slice<FreeCustomerResponseDTO>> getFreeCustomers(
		@Parameter(description = "페이징 정보 (page, size, sort)")
		@PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
		Pageable pageable
	) {
		return BaseResponse.onSuccess(customerQueryService.getFreeCustomers(pageable));
	}

	@Operation(
		summary = "신규 구독 고객 목록 조회",
		description = """
			신규로 구독한 고객 목록을 조회합니다.

			신규 구독 고객 정의:
			- ACTIVE 상태의 Subscription 보유
			- 다음 중 하나에 해당:
			  1. 구독 시작한지 24시간 이내 (Subscription.createdAt 기준)
			  2. 트레이너가 아직 배정되지 않은 구독 고객

			조회 정보:
			- 고객 기본 정보 (ID, 이름, 전화번호)
			- 레벨테스트 정보 (응시 여부, 상태, 채점 결과)
			- 상담 여부
			- 배정된 트레이너 정보

			레벨테스트 상태:
			- SUBMITTED: 제출됨
			- GRADING: 채점 중
			- GRADED: 채점 완료

			정렬:
			- 구독 시작일 내림차순 (최신순)

			페이징:
			- Slice 방식 (무한 스크롤)
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지 크기 (기본값: 20)

			예시:
			- GET /api/v1/admin/users/new-subscription-customers
			- GET /api/v1/admin/users/new-subscription-customers?page=0&size=20
			"""
	)
	@GetMapping("/new-subscription-customers")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<Slice<NewSubscriptionCustomerResponseDTO>> getNewSubscriptionCustomers(
		@Parameter(description = "페이징 정보 (page, size)")
		@PageableDefault(size = 20)
		Pageable pageable
	) {
		return BaseResponse.onSuccess(customerQueryService.getNewSubscriptionCustomers(pageable));
	}
}
