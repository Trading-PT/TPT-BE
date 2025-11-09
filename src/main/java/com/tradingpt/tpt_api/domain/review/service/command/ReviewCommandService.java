package com.tradingpt.tpt_api.domain.review.service.command;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReplyRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.UpdateReviewVisibilityRequestDTO;

public interface ReviewCommandService {

	/**
	 * 리뷰 생성
	 *
	 * @param customerId
	 * @param request
	 * @return
	 */
	Void createReview(Long customerId, CreateReviewRequestDTO request);

	/**
	 * 리뷰 답변 생성
	 *
	 * @param reviewId
	 * @param trainerId
	 * @param request
	 * @return
	 */
	Void createReply(Long reviewId, Long trainerId, CreateReplyRequestDTO request);

	/**
	 * 리뷰의 고객 공개 허용 여부를 변경
	 *
	 * @param reviewId
	 * @param request
	 * @return
	 */
	Void updateReviewVisibility(Long reviewId, UpdateReviewVisibilityRequestDTO request);

	/**
	 * 리뷰 답변 수정
	 */
	Void updateReply(Long reviewId, Long trainerId, CreateReplyRequestDTO request);

	/**
	 * 리뷰 삭제
	 */
	Void deleteReview(Long reviewId);

}
