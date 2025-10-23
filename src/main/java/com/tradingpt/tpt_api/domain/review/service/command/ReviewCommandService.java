package com.tradingpt.tpt_api.domain.review.service.command;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;

public interface ReviewCommandService {

	Void createReview(Long customerId, CreateReviewRequestDTO request);

}
