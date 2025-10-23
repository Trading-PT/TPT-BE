package com.tradingpt.tpt_api.domain.review.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.review.dto.response.PublicReviewListResponseDTO;
import com.tradingpt.tpt_api.domain.review.dto.response.ReviewResponseDTO;
import com.tradingpt.tpt_api.domain.review.entity.Review;
import com.tradingpt.tpt_api.domain.review.enums.Status;
import com.tradingpt.tpt_api.domain.review.repository.ReviewRepository;
import com.tradingpt.tpt_api.global.common.dto.SliceInfo;

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

	@Override
	public PublicReviewListResponseDTO getPublicReviews(Pageable pageable) {

		Slice<Review> reviewSlice = reviewRepository.findByStatusSlice(Status.PUBLIC, pageable);

		List<ReviewResponseDTO> reviews = reviewSlice.getContent()
			.stream()
			.map(ReviewResponseDTO.TrainerReplyResponseDTO::from)
			.toList();

		SliceInfo sliceInfo = SliceInfo.of(reviewSlice);

		return PublicReviewListResponseDTO.of(reviews, sliceInfo);
	}
}
