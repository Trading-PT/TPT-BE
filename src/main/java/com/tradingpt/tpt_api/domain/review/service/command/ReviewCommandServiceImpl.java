package com.tradingpt.tpt_api.domain.review.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReplyRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;
import com.tradingpt.tpt_api.domain.review.entity.Review;
import com.tradingpt.tpt_api.domain.review.exception.ReviewErrorStatus;
import com.tradingpt.tpt_api.domain.review.exception.ReviewException;
import com.tradingpt.tpt_api.domain.review.repository.ReviewRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.infrastructure.content.ContentImageUploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandServiceImpl implements ReviewCommandService {

	private final UserRepository userRepository;
	private final ContentImageUploader contentImageUploader;
	private final ReviewRepository reviewRepository;

	@Override
	public Void createReview(Long customerId, CreateReviewRequestDTO request) {

		// 유저 찾기
		Customer customer = (Customer)userRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 우선은 테스트를 위해 미구독 고객도 리뷰를 작성할 수 있도록 한다.

		// TODO: 구독 여부와 마지막 리뷰 작성일로부터 6개월이 지났을 경우에만 작성할 수 있도록 한다.

		// 리뷰 내용 업로드
		String processedContent = contentImageUploader.processContent(request.getContent(), "reviews");

		// 리뷰 생성 및 저장
		Review newReview = Review.createFrom(request, customer);
		reviewRepository.save(newReview);

		return null;
	}

	@Override
	public Void createReply(Long reviewId, Long trainerId, CreateReplyRequestDTO request) {

		// 리뷰 검색
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		// 트레이너 검색
		Trainer trainer = (Trainer)userRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

		// 답변 작성
		String processedContent = contentImageUploader.processContent(
			request.getContent(),
			"review-replies"
		);

		// 더티 체킹을 통한 리뷰 응답 저장
		review.addReply(trainer, processedContent);

		return null;
	}
}
