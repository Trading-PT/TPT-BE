package com.tradingpt.tpt_api.domain.user.controller;

import com.tradingpt.tpt_api.domain.user.dto.request.TrainerRequestDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.AssignedCustomerDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.TrainerListResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.TrainerResponseDTO;
import com.tradingpt.tpt_api.domain.user.service.command.AdminTrainerCommandService;
import com.tradingpt.tpt_api.domain.user.service.command.AdminTrainerQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/v1/admin/trainers")
@RequiredArgsConstructor
@Tag(name = "관리자(Admin) - 트레이너(Trainer) 관리", description = "관리자 전용 API")
public class AdminTrainerV1Controller {

	private final AdminTrainerCommandService adminTrainerCommandService;
	private final AdminTrainerQueryService adminTrainerQueryService;

	@Operation(summary = "트레이너 등록", description = "관리자만 트레이너 계정을 생성합니다. 프로필 이미지는 선택사항입니다.")
	@PostMapping(consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자 전용
	public ResponseEntity<BaseResponse<TrainerResponseDTO>> createTrainer(
			 @Valid @ModelAttribute TrainerRequestDTO request,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		TrainerResponseDTO trainerResponseDTO = adminTrainerCommandService.createTrainer(request, profileImage);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccessCreate(trainerResponseDTO));
	}

	@Operation(summary = "트레이너 수정", description = "관리자만 트레이너 정보를 일괄 수정합니다. (multipart/form-data, 이미지 선택)")
	@PutMapping(value = "/{trainerId}", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<TrainerResponseDTO>> updateTrainer(
			@PathVariable Long trainerId,
			@Valid @ModelAttribute TrainerRequestDTO request,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		TrainerResponseDTO dto = adminTrainerCommandService.updateTrainer(trainerId, request, profileImage);
		return ResponseEntity.ok(BaseResponse.onSuccess(dto));
	}

	@Operation(summary = "트레이너 삭제", description = "관리자만 트레이너를 영구 삭제합니다.")
	@DeleteMapping("/{trainerId}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<Void>> deleteTrainer(@PathVariable Long trainerId) {
		adminTrainerCommandService.deleteTrainer(trainerId);
		return ResponseEntity.ok(BaseResponse.onSuccess(null));
	}

	@Operation(summary = "관리자/트레이너 목록 조회", description = "프로필/성함/전화/한줄소개/ID/배정고객(트레이너만) 포함")
	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<List<TrainerListResponseDTO>>> getTrainers() {
		List<TrainerListResponseDTO> trainerListResponseDTO = adminTrainerQueryService.getTrainers();
		return ResponseEntity.ok(BaseResponse.onSuccess(trainerListResponseDTO));
	}
	@Operation(summary = "트레이너별 배정 고객 목록 조회",
			description = "특정 트레이너에게 배정된 고객 목록(고객 ID/이름)을 반환합니다.")
	@GetMapping("/{trainerId}/customers")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<List<AssignedCustomerDTO>>> getAssignedCustomers(
			@PathVariable Long trainerId
	) {
		List<AssignedCustomerDTO> assignedCustomerDTO  = adminTrainerQueryService.getAssignedCustomers(trainerId);
		return ResponseEntity.ok(BaseResponse.onSuccess(assignedCustomerDTO));
	}

	@Operation(summary = "고객의 배정 트레이너 변경",
			description = "특정 고객의 배정 트레이너를 path의 {trainerId}로 변경합니다.")
	@PutMapping("/{trainerId}/customers/{customerId}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<BaseResponse<Long>> reassignCustomerToTrainer(
			@PathVariable Long trainerId,
			@PathVariable Long customerId
	) {
		Long updatedCustomerId = adminTrainerCommandService.reassignCustomerToTrainer(trainerId, customerId);
		return ResponseEntity.ok(BaseResponse.onSuccess(updatedCustomerId));
	}
}
