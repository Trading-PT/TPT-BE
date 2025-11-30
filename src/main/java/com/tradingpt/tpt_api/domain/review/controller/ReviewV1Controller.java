package com.tradingpt.tpt_api.domain.review.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.PublicReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewStatisticsResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewTagResponseDTO;
import com.tradingpt.tpt_api.domain.review.service.command.ReviewCommandService;
import com.tradingpt.tpt_api.domain.review.service.query.ReviewQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "리뷰 관련 API")
public class ReviewV1Controller {

	private final ReviewQueryService reviewQueryService;
	private final ReviewCommandService reviewCommandService;

	@Operation(summary = "리뷰 작성", description = "리뷰를 작성합니다.")
	@PostMapping
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<Void> createReview(
		@RequestBody @Valid CreateReviewRequestDTO request,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccessCreate(reviewCommandService.createReview(customerId, request));
	}

	@Operation(
		summary = "내 리뷰 목록 조회",
		description = """
			로그인한 사용자가 자신이 작성한 리뷰 목록을 조회합니다.
			- 공개/비공개 리뷰 모두 조회 가능
			"""
	)
	@GetMapping("/me")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<List<ReviewResponseDTO>> getMyReviews(
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(reviewQueryService.getMyReviews(customerId));
	}

	@Operation(
		summary = "내 리뷰 상세 조회",
		description = """
			로그인한 사용자가 자신이 작성한 특정 리뷰를 상세 조회합니다.
			- 본인의 리뷰만 조회 가능
			- 공개/비공개 상관없이 조회 가능
			"""
	)
	@GetMapping("/me/{reviewId}")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<ReviewResponseDTO> getMyReview(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(reviewQueryService.getMyReview(reviewId, customerId));
	}

	@Operation(
		summary = "공개 리뷰 목록 조회 (무한 스크롤)",
		description = """
			모든 사용자(비회원 포함)가 공개된 리뷰 목록을 조회합니다.
			- 무한 스크롤 방식 (다음 페이지 여부만 제공)
			- 최신순으로 정렬
			- page: 페이지 번호 (0부터 시작, 기본값: 0)
			- size: 페이지 크기 (기본값: 12)
			"""
	)
	@GetMapping
	public BaseResponse<PublicReviewListResponseDTO> getPublicReviews(
		@PageableDefault(size = 12, sort = "submittedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		return BaseResponse.onSuccess(reviewQueryService.getPublicReviews(pageable));
	}

	@Operation(
		summary = "리뷰 상세 조회(공개용)",
		description = "특정 리뷰의 상세 정보를 조회합니다. (공개 리뷰만)"
	)
	@GetMapping("/{reviewId}")
	public BaseResponse<ReviewResponseDTO> getPublicReview(
		@PathVariable Long reviewId
	) {
		return BaseResponse.onSuccess(reviewQueryService.getPublicReview(reviewId));
	}

	@Operation(
		summary = "리뷰 태그 목록 조회",
		description = """
			리뷰 작성 시 선택 가능한 모든 태그 목록을 조회합니다.
			- 태그 이름 순으로 정렬
			- 누구나 조회 가능
			"""
	)
	@GetMapping("/tags")
	public BaseResponse<List<ReviewTagResponseDTO>> getReviewTags() {
		return BaseResponse.onSuccess(reviewQueryService.getReviewTags());
	}

	@Operation(
		summary = "리뷰 통계 조회",
		description = """
			리뷰 전체 통계를 조회합니다.
			- 전체 리뷰 개수
			- 평균 별점 (총 별점 합 / 총 리뷰 개수)
			- 태그별 리뷰 개수 (리뷰 수 내림차순 정렬)
			- 누구나 조회 가능
			"""
	)
	@GetMapping("/statistics")
	public BaseResponse<ReviewStatisticsResponseDTO> getReviewStatistics() {
		return BaseResponse.onSuccess(reviewQueryService.getReviewStatistics());
	}

}
