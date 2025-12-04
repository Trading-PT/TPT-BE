package com.tradingpt.tpt_api.domain.review.service.command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.review.dto.request.CreateReplyRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.CreateReviewRequestDTO;
import com.tradingpt.tpt_api.domain.review.dto.request.UpdateReviewVisibilityRequestDTO;
import com.tradingpt.tpt_api.domain.review.entity.Review;
import com.tradingpt.tpt_api.domain.review.entity.ReviewTag;
import com.tradingpt.tpt_api.domain.review.enums.Status;
import com.tradingpt.tpt_api.domain.review.exception.ReviewErrorStatus;
import com.tradingpt.tpt_api.domain.review.exception.ReviewException;
import com.tradingpt.tpt_api.domain.review.repository.ReviewRepository;
import com.tradingpt.tpt_api.domain.review.repository.ReviewTagRepository;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
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

	private static final Pattern S3_KEY_PATTERN = Pattern.compile("amazonaws\\.com/(.+)$");

	private final ContentImageUploader contentImageUploader;
	private final ReviewRepository reviewRepository;
	private final ReviewTagRepository reviewTagRepository;
	private final TrainerRepository trainerRepository;
	private final CustomerRepository customerRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;

	@Override
	public Void createReview(Long customerId, CreateReviewRequestDTO request) {

		// 유저 찾기
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 구독 상태 검증 - 활성 구독이 있어야만 리뷰 작성 가능
		// boolean hasActiveSubscription = subscriptionRepository
		// 	.findByCustomer_IdAndStatus(
		// 		customerId,
		// 		com.tradingpt.tpt_api.domain.subscription.enums.Status.ACTIVE
		// 	)
		// 	.isPresent();

		boolean hasActiveSubscription = customer.getMembershipLevel() == MembershipLevel.PREMIUM;

		if (!hasActiveSubscription) {
			throw new ReviewException(ReviewErrorStatus.SUBSCRIPTION_REQUIRED);
		}

		// 리뷰 내용 업로드 (인라인 이미지를 S3에 업로드하고 URL로 변환)
		String processedContent = contentImageUploader.processContent(request.getContent(), "reviews");

		// 리뷰 생성 및 저장
		Review newReview = Review.createFrom(customer, processedContent, request.getRating());
		reviewRepository.save(newReview);

		// 처리된 콘텐츠에서 S3 이미지 URL을 추출하여 ReviewAttachment 생성
		extractAndSaveAttachments(newReview, processedContent);

		// 태그 연결 (태그가 선택된 경우)
		if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
			List<ReviewTag> tags = reviewTagRepository.findAllByIdIn(request.getTagIds());
			newReview.addTags(tags);
		}

		return null;
	}

	/**
	 * 처리된 HTML에서 S3 이미지 URL을 추출하여 ReviewAttachment로 저장
	 */
	private void extractAndSaveAttachments(Review review, String processedContent) {
		Document document = Jsoup.parseBodyFragment(processedContent);

		for (Element img : document.select("img")) {
			String src = img.attr("src");

			// S3 URL인 경우에만 처리 (data: URI는 제외)
			if (src.contains("amazonaws.com") && !src.startsWith("data:")) {
				String fileKey = extractFileKeyFromUrl(src);
				if (fileKey != null) {
					review.addAttachment(src, fileKey);
					log.debug("Added ReviewAttachment: url={}, key={}", src, fileKey);
				}
			}
		}
	}

	/**
	 * S3 URL에서 파일 키를 추출
	 * 예: https://bucket.s3.region.amazonaws.com/reviews/inline-uuid.png -> reviews/inline-uuid.png
	 */
	private String extractFileKeyFromUrl(String url) {
		Matcher matcher = S3_KEY_PATTERN.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		log.warn("Could not extract file key from URL: {}", url);
		return null;
	}

	@Override
	public Void createReply(Long reviewId, Long adminId, CreateReplyRequestDTO request) {

		// 리뷰 검색
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorStatus.REVIEW_NOT_FOUND));

		// 리뷰가 이미 답변이 되어있으면 에러를 발생
		if (review.hasReply()) {
			throw new ReviewException(ReviewErrorStatus.REVIEW_ALREADY_HAS_REPLY);
		}

		// 어드민 검색
		User admin = userRepository.findById(adminId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 답변 작성
		String processedContent = contentImageUploader.processContent(
			request.getContent(),
			"review-replies"
		);

		// 더티 체킹을 통한 리뷰 응답 저장
		review.addReply(admin, processedContent);

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

		// 어드민 검색
		User user = userRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 답변 작성
		String processedContent = contentImageUploader.processContent(
			request.getContent(),
			"review-replies"
		);

		// 더티 체킹을 통한 리뷰 응답 저장
		review.addReply(user, processedContent);

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
