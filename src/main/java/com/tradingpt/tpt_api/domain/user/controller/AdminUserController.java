package com.tradingpt.tpt_api.domain.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.user.dto.response.PendingUserApprovalRowResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.UserStatusUpdateResponseDTO;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.service.command.AdminUserCommandService;
import com.tradingpt.tpt_api.domain.user.service.query.AdminUserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("admin/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "관리자 - 회원 관리", description = "관리자/트레이너 전용 회원 관리 API")
public class AdminUserController {

	private final AdminUserCommandService adminUserCommandService;
	private final AdminUserQueryService adminUserQueryService;

	@Operation(summary = "신규 가입자 목록(UID 검토 중) 조회")
	@GetMapping("/pending")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public ResponseEntity<List<PendingUserApprovalRowResponseDTO>> getPendingUsers() {
		return ResponseEntity.ok(adminUserQueryService.getPendingApprovalRows());
	}

	@Operation(summary = "신규 가입자 UID 승인 여부 처리 (승인/거절)")
	@PatchMapping("/{userId}/status")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public ResponseEntity<UserStatusUpdateResponseDTO> updateUserStatus(
		@PathVariable Long userId,
		@RequestParam UserStatus status
	) {
		adminUserCommandService.updateUserStatus(userId, status);
		return ResponseEntity.ok(UserStatusUpdateResponseDTO.of(userId));
	}

}
