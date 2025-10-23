package com.tradingpt.tpt_api.domain.review.service.command;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReplyRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;

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
}
