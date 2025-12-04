package com.tradingpt.tpt_api.domain.feedbackresponse.service.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.request.CreateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.request.UpdateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.response.FeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.infrastructure.content.ContentImageUploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 피드백 답변 Command Service 구현체
 * 피드백 답변 생성, 수정과 관련된 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackResponseCommandServiceImpl implements FeedbackResponseCommandService {

	private static final Pattern S3_KEY_PATTERN = Pattern.compile("amazonaws\\.com/(.+)$");

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final UserRepository userRepository;
	private final ContentImageUploader contentImageUploader;

	@Override
	public FeedbackResponseDTO createFeedbackResponse(
		Long feedbackRequestId,
		CreateFeedbackResponseRequestDTO request,
		Long trainerId
	) {
		log.info("Creating feedback response for feedbackRequestId={}, trainerId={}",
			feedbackRequestId, trainerId);

		// 1. 피드백 요청 조회
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 2. 이미 응답이 존재하는지 확인
		if (feedbackRequest.getFeedbackResponse() != null) {
			log.warn("Feedback response already exists for feedbackRequestId={}", feedbackRequestId);
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_RESPONSE_ALREADY_EXISTS);
		}

		// 3. 작성자 조회 (Trainer 또는 Admin)
		User writer = getWriterById(trainerId);

		validateWriterPermission(feedbackRequest, writer);

		// 4. ✅ 콘텐츠 처리 (저장 전에 처리 완료)
		String processedContent = contentImageUploader.processContent(
			request.getContent(),
			"feedback-responses"
		);

		// 5. 피드백 응답 생성 (처리된 콘텐츠로)
		FeedbackResponse feedbackResponse = FeedbackResponse.createFrom(
			feedbackRequest,
			writer,
			request.getTitle(),
			processedContent
		);

		// 6. 이미지 첨부파일 추출 및 저장
		extractAndSaveAttachments(feedbackResponse, processedContent);

		// 7. 피드백 상태 업데이트 (FN: 응답 완료, 아직 읽지 않음)
		feedbackRequest.setStatus(Status.FN);

		// 8. 저장 (cascade로 FeedbackResponse와 Attachment도 함께 저장됨)
		feedbackRequestRepository.save(feedbackRequest);

		log.info("Feedback response created successfully for feedbackRequestId={}", feedbackRequestId);

		return FeedbackResponseDTO.of(feedbackResponse, writer);
	}

	@Override
	public FeedbackResponseDTO updateFeedbackResponse(
		Long feedbackRequestId,
		UpdateFeedbackResponseRequestDTO request,
		Long trainerId
	) {
		log.info("Updating feedback response for feedbackRequestId={}, trainerId={}",
			feedbackRequestId, trainerId);

		// 1. 피드백 요청 조회
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 2. 피드백 응답 존재 확인
		FeedbackResponse feedbackResponse = feedbackRequest.getFeedbackResponse();
		if (feedbackResponse == null) {
			log.warn("Feedback response not found for feedbackRequestId={}", feedbackRequestId);
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_RESPONSE_NOT_FOUND);
		}

		// 3. 권한 확인 (작성자만 수정 가능)
		if (!feedbackResponse.getWriter().getId().equals(trainerId)) {
			log.warn("User {} attempted to update response created by user {}",
				trainerId, feedbackResponse.getWriter().getId());
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.RESPONSE_UPDATE_PERMISSION_DENIED);
		}

		// 4. 작성자 조회
		User writer = getWriterById(trainerId);

		// 5. ✅ 콘텐츠 처리 (저장 전에 처리 완료)
		String processedContent = contentImageUploader.processContent(
			request.getContent(),
			"feedback-responses"
		);

		// 6. 피드백 응답 업데이트
		feedbackResponse.updateContent(processedContent);

		// 7. 기존 첨부파일 삭제 후 새로 추출
		feedbackResponse.clearAttachments();
		extractAndSaveAttachments(feedbackResponse, processedContent);

		log.info("Feedback response updated successfully for feedbackRequestId={}", feedbackRequestId);

		return FeedbackResponseDTO.of(feedbackResponse, writer);
	}

	// ========================================
	// Private Helper Methods
	// ========================================

	/**
	 * ✅ 작성자 권한 검증 (Trainer 또는 Admin)
	 * - Admin: 모든 피드백에 응답 가능
	 * - Trainer + 토큰 사용 피드백: 모든 트레이너 응답 가능
	 * - Trainer + 일반 피드백: 배정된 트레이너만 응답 가능
	 */
	private void validateWriterPermission(FeedbackRequest feedbackRequest, User writer) {
		// Admin은 모든 피드백에 응답 가능
		if (writer.getRole() == Role.ROLE_ADMIN) {
			log.info("Admin user, can respond to any feedback");
			return;
		}

		// 토큰 사용 피드백이면 모든 트레이너 응답 가능
		if (Boolean.TRUE.equals(feedbackRequest.getIsTokenUsed())) {
			log.info("Token-used feedback, any user can respond");
			return;
		}

		// 일반 피드백이면 배정된 트레이너만 응답 가능
		Customer customer = feedbackRequest.getCustomer();
		if (customer.getAssignedTrainer() == null
			|| !customer.getAssignedTrainer().getId().equals(writer.getId())) {
			log.warn("Trainer not assigned to this customer: trainerId={}, customerId={}",
				writer.getId(), customer.getId());
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.CANNOT_RESPOND_TO_NON_TOKEN_FEEDBACK_AS_UNASSIGNED_TRAINER);
		}
	}

	/**
	 * 작성자 조회 헬퍼 메서드 (Trainer 또는 Admin)
	 */
	private User getWriterById(Long writerId) {
		User user = userRepository.findById(writerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// Trainer 또는 Admin만 피드백 응답 작성 가능
		if (user.getRole() != Role.ROLE_TRAINER && user.getRole() != Role.ROLE_ADMIN) {
			throw new UserException(UserErrorStatus.TRAINER_NOT_FOUND);
		}

		return user;
	}

	/**
	 * 처리된 콘텐츠에서 S3 이미지 URL을 추출하여 FeedbackResponseAttachment로 저장
	 */
	private void extractAndSaveAttachments(FeedbackResponse feedbackResponse, String processedContent) {
		Document document = Jsoup.parseBodyFragment(processedContent);

		for (Element img : document.select("img")) {
			String src = img.attr("src");

			// S3 URL인 경우만 처리 (base64 제외)
			if (src.contains("amazonaws.com") && !src.startsWith("data:")) {
				String fileKey = extractFileKeyFromUrl(src);
				if (fileKey != null) {
					feedbackResponse.addAttachment(src, fileKey);
					log.debug("Added FeedbackResponseAttachment: url={}, key={}", src, fileKey);
				}
			}
		}
	}

	/**
	 * S3 URL에서 파일 키 추출
	 */
	private String extractFileKeyFromUrl(String url) {
		Matcher matcher = S3_KEY_PATTERN.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		log.warn("Could not extract file key from URL: {}", url);
		return null;
	}
}