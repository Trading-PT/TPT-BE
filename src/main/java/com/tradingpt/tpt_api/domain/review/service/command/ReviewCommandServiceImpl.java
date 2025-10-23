package com.tradingpt.tpt_api.domain.review.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;
import com.tradingpt.tpt_api.domain.review.entity.Review;
import com.tradingpt.tpt_api.domain.review.repository.ReviewRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
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
}
