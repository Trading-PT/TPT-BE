package com.tradingpt.tpt_api.domain.review.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;
import com.tradingpt.tpt_api.domain.review.entity.Review;
import com.tradingpt.tpt_api.domain.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryServiceImpl implements ReviewQueryService {

	private final ReviewRepository reviewRepository;

	@Override
	public List<ReviewResponseDTO> getMyReviews(Long customerId) {
		List<Review> reviews = reviewRepository.findByCustomerIdOrderBySubmittedAtDesc(
			customerId);

		return reviews.stream()
			.map(ReviewResponseDTO.TrainerReplyResponseDTO::from)
			.toList();
	}
}
