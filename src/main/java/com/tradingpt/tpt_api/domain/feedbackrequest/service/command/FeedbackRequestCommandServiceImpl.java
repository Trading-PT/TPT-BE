package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateFeedbackRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.UpdateFeedbackRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.UpdateTrainerWrittenResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.common.RewardConstants;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3UploadResult;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackRequestCommandServiceImpl implements FeedbackRequestCommandService {

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final UserRepository userRepository;
	private final S3FileService s3FileService;

	@Override
	public FeedbackRequestDetailResponseDTO createFeedbackRequest(CreateFeedbackRequestDTO request, Long customerId) {
		Customer customer = getCustomerById(customerId);

		// ✅ 사용자의 트레이딩 타입 체크 (DDD: Entity에서 검증)
		customer.checkTradingType(request.getInvestmentType());

		// ✅ courseStatus 검증 (DDD: Entity에서 검증)
		customer.validateCourseStatusCompatibility(request.getCourseStatus());

		// ✅ 토큰 검증 및 차감 (DDD: Entity에서 검증 및 상태 변경)
		boolean tokenConsumed = customer.validateAndConsumeTokenForFeedback(
			request.getUseToken(),
			RewardConstants.DEFAULT_TOKEN_CONSUMPTION
		);

		// ✅ FeedbackRequest 생성 (DDD: Entity Factory Method 활용)
		FeedbackRequest feedbackRequest = FeedbackRequest.createFrom(request, customer);

		// ⭐ 스크린샷 업로드 (공통 메서드 사용)
		uploadScreenshots(request.getScreenshotFiles(), feedbackRequest);

		// ✅ 토큰 사용 여부 설정
		if (tokenConsumed) {
			feedbackRequest.useToken(RewardConstants.DEFAULT_TOKEN_CONSUMPTION);
			log.info("Feedback request created with token: customerId={}, tokenAmount={}",
				customerId, RewardConstants.DEFAULT_TOKEN_CONSUMPTION);
		} else {
			log.info("Feedback request created as record-only (no token): customerId={}", customerId);
		}

		// CASCADE 설정으로 FeedbackRequest 저장 시 attachment도 자동 저장됨
		FeedbackRequest saved = feedbackRequestRepository.save(feedbackRequest);

		// ⭐ 피드백 카운트 증가 및 토큰 보상 (DDD 패턴)
		customer.incrementFeedbackCount();
		boolean rewarded = customer.rewardTokensIfEligible(
			RewardConstants.FEEDBACK_THRESHOLD,
			RewardConstants.TOKEN_REWARD_AMOUNT
		);

		if (rewarded) {
			log.info("🎉 Token reward milestone reached! customerId={}, feedbackCount={}, tokensEarned={}, totalTokens={}",
				customerId,
				customer.getFeedbackRequestCount(),
				RewardConstants.TOKEN_REWARD_AMOUNT,
				customer.getToken());
		}

		// JPA Dirty Checking이 자동으로 Customer UPDATE (save() 불필요)

		return FeedbackRequestDetailResponseDTO.from(saved);
	}

	@Override
	public FeedbackRequestDetailResponseDTO updateFeedbackRequest(
		Long feedbackRequestId,
		UpdateFeedbackRequestDTO request,
		Long customerId
	) {
		// 1. 피드백 요청 조회
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 2. 소유권 검증 (DDD: Entity에서 검증)
		feedbackRequest.validateOwnership(customerId);

		// 3. 수정 가능 상태 검증 (DDD: Entity에서 검증)
		feedbackRequest.validateUpdatable();

		// 4. 매매 기본 정보 업데이트 (DDD: Entity 비즈니스 메서드)
		feedbackRequest.updateTradingData(
			request.getCategory(),
			request.getPositionHoldingTime(),
			request.getPosition(),
			request.getPnl(),
			request.getTotalAssetPnl(),
			request.getRnr(),
			request.getRiskTaking(),
			request.getLeverage(),
			request.getOperatingFundsRatio(),
			request.getEntryPrice(),
			request.getExitPrice(),
			request.getSettingStopLoss(),
			request.getSettingTakeProfit(),
			request.getPositionStartReason(),
			request.getPositionEndReason(),
			request.getTradingReview()
		);

		// 5. 완강 후 전용 필드 업데이트 (해당 시)
		feedbackRequest.updateAfterCompletionData(
			request.getDirectionFrameExists(),
			request.getDirectionFrame(),
			request.getMainFrame(),
			request.getSubFrame(),
			request.getTrendAnalysis(),
			request.getTrainerFeedbackRequestContent(),
			request.getEntryPoint(),
			request.getGrade(),
			request.getAdditionalBuyCount(),
			request.getSplitSellCount()
		);

		// 6. SWING 전용 필드 업데이트 (해당 시)
		feedbackRequest.updateSwingSpecificData(
			request.getPositionStartDate(),
			request.getPositionEndDate()
		);

		log.info("Feedback request updated: feedbackRequestId={}, customerId={}",
			feedbackRequestId, customerId);

		// JPA Dirty Checking이 자동으로 UPDATE 처리 (save() 불필요)
		return FeedbackRequestDetailResponseDTO.from(feedbackRequest);
	}

	@Override
	public Void deleteFeedbackRequest(Long feedbackRequestId, Long customerId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 권한 확인: 자신의 피드백 요청만 삭제 가능
		if (!feedbackRequest.getCustomer().getId().equals(customerId)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.DELETE_PERMISSION_DENIED);
		}

		// ✅ 누적 작성 횟수는 삭제 시에도 감소하지 않음 (총 몇 개를 작성했는지만 카운트)
		// 피드백 카운트는 단조증가하므로 decrementFeedbackCount() 호출 제거

		feedbackRequestRepository.delete(feedbackRequest);

		log.info("Feedback deleted: feedbackRequestId={}, customerId={}",
			feedbackRequestId, customerId);

		return null;
	}

	@Override
	public Void deleteByAdmin(Long feedbackRequestId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// Admin은 소유권 검증 없이 모든 피드백 삭제 가능
		feedbackRequestRepository.delete(feedbackRequest);

		log.info("Feedback deleted by admin: feedbackRequestId={}, customerId={}",
			feedbackRequestId, feedbackRequest.getCustomer().getId());

		return null;
	}

	@Override
	public Void updateBestFeedbacks(List<Long> feedbackIds) {
		// 1. 개수 검증 (최대 개수는 FeedbackRequest.MAX_BEST_FEEDBACK_COUNT)
		if (feedbackIds.size() > FeedbackRequest.MAX_BEST_FEEDBACK_COUNT) {
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.BEST_FEEDBACK_LIMIT_EXCEEDED
			);
		}

		// 2. 기존 베스트 피드백 모두 해제
		List<FeedbackRequest> currentBestFeedbacks = feedbackRequestRepository
			.findByIsBestFeedbackTrue();

		currentBestFeedbacks.forEach(feedback ->
			feedback.updateIsBestFeedback(false)
		);

		// 3. 빈 배열이면 여기서 종료 (모든 베스트 해제만)
		if (feedbackIds.isEmpty()) {
			log.info("All best feedbacks have been cleared");
			return null;
		}

		// 4. 새로운 베스트 피드백 지정
		List<FeedbackRequest> newBestFeedbacks = feedbackRequestRepository
			.findAllById(feedbackIds);

		// 5. 요청된 ID가 모두 존재하는지 확인
		if (newBestFeedbacks.size() != feedbackIds.size()) {
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND
			);
		}

		// 6. 베스트로 지정
		newBestFeedbacks.forEach(feedback ->
			feedback.updateIsBestFeedback(true)
		);

		log.info("Best feedbacks updated: {} feedbacks selected", newBestFeedbacks.size());

		return null;
	}

	@Override
	public UpdateTrainerWrittenResponseDTO updateTrainerWrittenFeedbacks(List<Long> feedbackRequestIds) {
		// 1. 피드백 일괄 조회
		List<FeedbackRequest> feedbacks = feedbackRequestRepository.findAllById(feedbackRequestIds);

		// 2. 요청된 ID가 모두 존재하는지 확인
		if (feedbacks.size() != feedbackRequestIds.size()) {
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND
			);
		}

		// 3. 트레이너 작성으로 표시 (Entity 비즈니스 메서드 활용)
		List<Long> updatedIds = feedbacks.stream()
			.peek(FeedbackRequest::markAsTrainerWritten)
			.map(FeedbackRequest::getId)
			.toList();

		log.info("Trainer-written feedbacks updated: {} feedbacks marked", updatedIds.size());

		// JPA Dirty Checking이 자동으로 UPDATE 처리 (save() 불필요)
		return UpdateTrainerWrittenResponseDTO.from(updatedIds);
	}

	// ========================================
	// Private Helper Methods
	// ========================================

	/**
	 * 스크린샷 파일들을 S3에 업로드하고 FeedbackRequestAttachment를 생성한다.
	 *
	 * @param screenshotFiles 업로드할 스크린샷 파일 리스트
	 * @param feedbackRequest 첨부될 피드백 요청 엔티티
	 */
	private void uploadScreenshots(List<MultipartFile> screenshotFiles, FeedbackRequest feedbackRequest) {
		if (screenshotFiles == null || screenshotFiles.isEmpty()) {
			return;
		}

		for (MultipartFile screenshotFile : screenshotFiles) {
			if (screenshotFile != null && !screenshotFile.isEmpty()) {
				S3UploadResult uploadResult = s3FileService.upload(screenshotFile, "feedback-requests/screenshots");
				FeedbackRequestAttachment.createFrom(feedbackRequest, uploadResult.url(), uploadResult.key());
			}
		}
	}


	/**
	 * Customer ID로 Customer 엔티티를 조회한다.
	 *
	 * @param customerId 조회할 고객 ID
	 * @return 조회된 Customer 엔티티
	 * @throws UserException 고객을 찾을 수 없는 경우
	 */
	private Customer getCustomerById(Long customerId) {
		return (Customer)userRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));
	}


}
