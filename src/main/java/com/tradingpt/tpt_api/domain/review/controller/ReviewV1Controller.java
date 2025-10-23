package com.tradingpt.tpt_api.domain.review.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;
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

	@Operation(summary = "내 리뷰 내역 보기", description = "내가 작성한 리뷰들을 봅니다.")
	@GetMapping
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public BaseResponse<List<ReviewResponseDTO>> getMyReviews(
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {
		return BaseResponse.onSuccess(reviewQueryService.getMyReviews(customerId));
	}
	
}
