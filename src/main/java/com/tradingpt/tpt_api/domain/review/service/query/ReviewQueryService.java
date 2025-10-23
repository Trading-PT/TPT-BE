package com.tradingpt.tpt_api.domain.review.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.tradingpt.tpt_api.domain.review.dto.response.PublicReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;

public interface ReviewQueryService {

	List<ReviewResponseDTO> getMyReviews(Long customerId);

	PublicReviewListResponseDTO getPublicReviews(Pageable pageable);
}
