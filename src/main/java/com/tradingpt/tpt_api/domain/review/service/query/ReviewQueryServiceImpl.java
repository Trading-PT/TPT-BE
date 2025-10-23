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
import com.tradingpt.tpt_api.domain.review.exception.ReviewErrorStatus;
import com.tradingpt.tpt_api.domain.review.exception.ReviewException;
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
			.map(ReviewResponseDTO::from)
			.toList();
	}

	@Override
	public ReviewResponseDTO getMyReview(Long reviewId, Long customerId) {
		// 리뷰 조회
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		// 본인 리뷰인지 확인
		if (!review.getCustomer().getId().equals(customerId)) {
			throw new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND);
		}

		// 본인 리뷰면 공개/비공개 상관없이 조회 가능
		return ReviewResponseDTO.from(review);
	}

	@Override
	public PublicReviewListResponseDTO getPublicReviews(Pageable pageable) {

		Slice<Review> reviewSlice = reviewRepository.findByStatusSlice(Status.PUBLIC, pageable);

		List<ReviewResponseDTO> reviews = reviewSlice.getContent()
			.stream()
			.map(ReviewResponseDTO::from)
			.toList();

		SliceInfo sliceInfo = SliceInfo.of(reviewSlice);

		return PublicReviewListResponseDTO.of(reviews, sliceInfo);
	}

	@Override
	public ReviewResponseDTO getPublicReview(Long reviewId) {
		Review review = reviewRepository.findByIdAndStatus(reviewId, Status.PUBLIC)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		return ReviewResponseDTO.from(review);
	}
}
