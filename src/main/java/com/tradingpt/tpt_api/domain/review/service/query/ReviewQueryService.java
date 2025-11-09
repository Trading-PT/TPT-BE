package com.tradingpt.tpt_api.domain.review.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.tradingpt.tpt_api.domain.review.dto.response.AdminReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.PublicReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;

public interface ReviewQueryService {

	/**
	 * 내 리뷰 목록 조회
	 */
	List<ReviewResponseDTO> getMyReviews(Long customerId);

	/**
	 * 내 리뷰 상세 조회
	 */
	ReviewResponseDTO getMyReview(Long reviewId, Long customerId);

	/**
	 * 공개 리뷰 목록 조회
	 */
	PublicReviewListResponseDTO getPublicReviews(Pageable pageable);

	/**
	 * 공개 리뷰 상세 조회
	 */
	ReviewResponseDTO getPublicReview(Long reviewId);

	/**
	 * 어드민용 리뷰 목록 조회
	 */
	AdminReviewListResponseDTO getReviews(Pageable pageable);

	/**
	 * 어드민용 리뷰 상세 조회
	 */
	ReviewResponseDTO getReview(Long reviewId);

}
