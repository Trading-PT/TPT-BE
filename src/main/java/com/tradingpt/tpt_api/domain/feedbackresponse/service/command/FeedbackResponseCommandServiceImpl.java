package com.tradingpt.tpt_api.domain.feedbackresponse.service.command;

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
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
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

		// 3. 트레이너 조회
		Trainer trainer = getTrainerById(trainerId);

		// 4. ✅ 콘텐츠 처리 (저장 전에 처리 완료)
		String processedContent = contentImageUploader.processContent(
			request.getContent(),
			"feedback-responses"
		);

		// 5. 피드백 응답 생성 (처리된 콘텐츠로)
		FeedbackResponse feedbackResponse = FeedbackResponse.createFrom(
			feedbackRequest,
			trainer,
			request.getTitle(),
			processedContent
		);

		// 6. 피드백 상태 업데이트 (FN: 응답 완료, 아직 읽지 않음)
		feedbackRequest.setStatus(Status.FN);

		// 7. 저장 (cascade로 FeedbackResponse도 함께 저장됨)
		feedbackRequestRepository.save(feedbackRequest);

		log.info("Feedback response created successfully for feedbackRequestId={}", feedbackRequestId);

		return FeedbackResponseDTO.of(feedbackResponse, trainer);
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
		if (!feedbackResponse.getTrainer().getId().equals(trainerId)) {
			log.warn("Trainer {} attempted to update response created by trainer {}",
				trainerId, feedbackResponse.getTrainer().getId());
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.RESPONSE_UPDATE_PERMISSION_DENIED);
		}

		// 4. 트레이너 조회
		Trainer trainer = getTrainerById(trainerId);

		// 5. ✅ 콘텐츠 처리 (저장 전에 처리 완료)
		String processedContent = contentImageUploader.processContent(
			request.getContent(),
			"feedback-responses"
		);

		// 6. 피드백 응답 업데이트
		feedbackResponse.updateContent(processedContent);

		log.info("Feedback response updated successfully for feedbackRequestId={}", feedbackRequestId);

		return FeedbackResponseDTO.of(feedbackResponse, trainer);
	}

	/**
	 * 트레이너 조회 헬퍼 메서드
	 */
	private Trainer getTrainerById(Long trainerId) {
		return (Trainer)userRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));
	}
}