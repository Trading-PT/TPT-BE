package com.tradingpt.tpt_api.domain.review.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReplyRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.UpdateReviewVisibilityRequestDTO;
import com.tradingpt.tpt_api.domain.review.service.command.ReviewCommandService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 관리", description = "리뷰 답변 관련 API (Admin/Trainer)")
public class AdminReviewV1Controller {

	private final ReviewCommandService reviewCommandService;

	@Operation(
		summary = "리뷰 답변 작성",
		description = """
			리뷰에 답변을 작성합니다.
			- Admin & Trainer: 모든 리뷰에 답변 가능
			- 이미 답변이 있는 경우 에러 반환
			"""
	)
	@PostMapping("/{reviewId}/reply")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<Void> createReply(
		@PathVariable Long reviewId,
		@RequestBody @Valid CreateReplyRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccessCreate(reviewCommandService.createReply(reviewId, trainerId, request));
	}

	@Operation(
		summary = "리뷰 공개 여부 변경",
		description = """
			리뷰의 공개 여부를 설정합니다.
			- Admin & Trainer: 모든 리뷰의 고객 공개 허용 여부를 설정할 수 있다.
			- true: 공개, false: 비공개
			"""
	)
	@PatchMapping("/{reviewId}/visibility")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<Void> updateReviewVisibility(
		@PathVariable Long reviewId,
		@RequestBody @Valid UpdateReviewVisibilityRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(reviewCommandService.updateReviewVisibility(reviewId, request));
	}
}
