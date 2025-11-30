package com.tradingpt.tpt_api.domain.review.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.tradingpt.tpt_api.domain.review.dto.response.AdminReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.PublicReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewStatisticsResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewTagResponseDTO;

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

	/**
	 * 리뷰 태그 목록 조회
	 * 리뷰 작성 시 선택 가능한 모든 태그 목록을 반환합니다.
	 */
	List<ReviewTagResponseDTO> getReviewTags();

	/**
	 * 리뷰 통계 조회 (공개용)
	 * 전체 리뷰 개수, 평균 별점, 태그별 통계를 반환합니다.
	 */
	ReviewStatisticsResponseDTO getReviewStatistics();

}
