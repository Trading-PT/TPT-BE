package com.tradingpt.tpt_api.domain.review.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReplyRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.UpdateReviewVisibilityRequestDTO;
import com.tradingpt.tpt_api.domain.review.entity.Review;
import com.tradingpt.tpt_api.domain.review.enums.Status;
import com.tradingpt.tpt_api.domain.review.exception.ReviewErrorStatus;
import com.tradingpt.tpt_api.domain.review.exception.ReviewException;
import com.tradingpt.tpt_api.domain.review.repository.ReviewRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
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
	private final TrainerRepository trainerRepository;

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

		// 리뷰가 이미 답변이 되어있으면 에러를 발생
		if (review.hasReply()) {
			throw new ReviewException(ReviewErrorStatus.REVIEW_ALREADY_HAS_REPLY);
		}

		// 트레이너 검색
		Trainer trainer = trainerRepository.findById(trainerId)
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

	@Override
	public Void updateReviewVisibility(Long reviewId, UpdateReviewVisibilityRequestDTO request) {
		// 리뷰 검색
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		review.updateVisibility(request.getIsPublic() == true ? Status.PUBLIC : Status.PRIVATE);

		return null;
	}

	@Override
	public Void updateReply(Long reviewId, Long trainerId, CreateReplyRequestDTO request) {
		// 리뷰 검색
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		// 리뷰에 답변이 달려있지 않다면 에러를 발생
		if (!review.hasReply()) {
			throw new ReviewException(ReviewErrorStatus.REVIEW_HAS_NO_REPLY);
		}

		// 트레이너 검색
		Trainer trainer = trainerRepository.findById(trainerId)
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

	@Override
	public Void deleteReview(Long reviewId) {
		// 리뷰 검색
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		// 리뷰 삭제
		reviewRepository.delete(review);

		return null;
	}

	@Override
	public Void deleteReply(Long reviewId) {
		// 리뷰 검색
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		// 리뷰에 답변이 없다면 에러 발생
		if (!review.hasReply()) {
			throw new ReviewException(ReviewErrorStatus.REVIEW_HAS_NO_REPLY);
		}

		// 리뷰의 content 및 답변을 단 trainer을 모두 null로 변경
		review.addReply(null, null);

		return null;
	}
}
