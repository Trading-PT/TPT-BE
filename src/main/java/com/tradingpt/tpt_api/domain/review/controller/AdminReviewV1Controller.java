package com.tradingpt.tpt_api.domain.review.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReplyRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.UpdateReviewVisibilityRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.AdminReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;
import com.tradingpt.tpt_api.domain.review.service.command.ReviewCommandService;
import com.tradingpt.tpt_api.domain.review.service.query.ReviewQueryService;
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

	private final ReviewQueryService reviewQueryService;
	private final ReviewCommandService reviewCommandService;

	@Operation(
		summary = "리뷰 전체 조회",
		description = """
			관리자가 리뷰 전체 목록을 봅니다.
			- Admin & Trainer: 모든 리뷰 확인 가능
			- 무한 스크롤 방식 (다음 페이지 여부만 제공)
			- 최신순으로 정렬
			- page: 페이지 번호 (0부터 시작, 기본값: 0)
			- size: 페이지 크기 (기본값: 12)
			"""
	)
	@GetMapping
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<AdminReviewListResponseDTO> getReviews(
		@PageableDefault(size = 12, sort = "submittedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		return BaseResponse.onSuccess(
			reviewQueryService.getReviews(pageable)
		);
	}

	@Operation(
		summary = "리뷰 상세 조회",
		description = """
			관리자가 리뷰 상세 조회를 합니다.
			- Admin & Trainer: 모든 리뷰 확인 가능
			"""
	)
	@GetMapping("/{reviewId}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<ReviewResponseDTO> getReviewDetail(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(reviewQueryService.getReview(reviewId));
	}

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
		summary = "리뷰 내용 수정",
		description = """
			리뷰에 대해 트레이너의 답변이 이미 작성되어 있으면 답변 내용을 수정합니다.
			- 기존에 답변이 달려있지 않은 경우 에러 반환
			"""
	)
	@PatchMapping("/{reviewId}/reply")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
	public BaseResponse<Void> updateReply(
		@PathVariable Long reviewId,
		@RequestBody @Valid CreateReplyRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long trainerId
	) {
		return BaseResponse.onSuccess(reviewCommandService.updateReply(reviewId, trainerId, request));
	}

	@Operation(
		summary = "리뷰 삭제",
		description = """
			리뷰를 삭제합니다.
			- Admin Only
			"""
	)
	@DeleteMapping("/{reviewId}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public BaseResponse<Void> deleteReview(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal(expression = "id") Long adminId
	) {
		return BaseResponse.onSuccessDelete(reviewCommandService.deleteReview(reviewId));
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
