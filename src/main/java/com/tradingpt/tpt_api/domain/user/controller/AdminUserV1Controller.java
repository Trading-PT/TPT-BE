package com.tradingpt.tpt_api.domain.user.controller;

import com.tradingpt.tpt_api.domain.user.dto.request.UidUpdateRequestDTO;
import java.util.List;

import org.springframework.data.domain.Pageable;
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
import com.tradingpt.tpt_api.domain.user.dto.response.MyCustomerListResponseDTO;
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
}
